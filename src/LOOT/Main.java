package LOOT;

/*

just follow that and stop trying to make keylogger noob LOLOLOL that was years and years ago
back when i coded in VB :P

well it's clear u don't know anything bout coding

stop trying to do states whats wrong with states :c

just garbage
 */




import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.Players;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Player;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.input.mouse.MainScreenTileDestination;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.api.model.*;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

@ScriptManifest(name = "GE LOOTER", author = "Pat and Mac", version = 1.0, info = "Goes to GE and Loots from Dead players", logo = "")
public class Main extends Script {
    private State currentState = State.REVIVED;
    public static Area getLocationArea = Banks.GRAND_EXCHANGE;
    private final Area bankingArea = new Area(3225, 3212, 3218, 3225);
    private Position DrawTile = new Position(1, 2, 3);
    private final String varrockTeleport = "Varrock teleport";
    private ConditionalSleep whileTravelingToBank = new ConditionalSleep((int) (Math.random() * 1000.0D + 2000.0D)) {
        @Override
        public boolean condition() throws InterruptedException {
            return bankingArea.contains(myPosition());
        }
    };



    private void bank() throws InterruptedException {
        super.log("At beginning of bank loop. currentState: " + this.currentState);
        /* state 1: revived from death */
        if (this.currentState == State.REVIVED) {
            log("Executing Revived");

            // if with walking distance of bank, walk to bank
            boolean inBankingArea = !bankingArea.contains(myPosition());
            super.log("inBankingArea: " + inBankingArea);
            if (inBankingArea) {
                boolean walkingTowardsBank = super.getWalking().walk(bankingArea);
                super.log("walkingTowardsBank: " + walkingTowardsBank);
                if (walkingTowardsBank) {
                    this.currentState = State.TRAVELINGTOBANKCHEST;
                }
            } else {
                this.currentState = State.ATBANKCHEST;
            }
        } else if (this.currentState == State.TRAVELINGTOBANKCHEST) {
            super.log("entered Traveling To Bank Chest");
            this.whileTravelingToBank.sleep();

            if (bankingArea.contains(myPosition())) {
                this.currentState = State.ATBANKCHEST;
            }
        } else if (this.currentState == State.ATBANKCHEST) {
            if (!getBank().isOpen()) {
                boolean bankOpenSucceeded = getBank().open();
                if (bankOpenSucceeded) {
                    this.currentState = State.ATBANKPROMPT;
                }
            }
        } else if (this.currentState == State.ATBANKPROMPT) {
            //check if the bank contains teleport
            if (getBank().contains(varrockTeleport)) {
                boolean withdrawSucceeded = getBank().withdraw(varrockTeleport, 1);
                if (withdrawSucceeded) {
                    boolean bankClosed = getBank().close();

                    if (bankClosed) {
                        this.currentState = State.HASTELEPORT;
                    }
                }
            }
        } else if (this.currentState == State.HASTELEPORT) {
            //check inventory for teleport
            if (inventory.contains(varrockTeleport)) {
                Item teleport = inventory.getItem(varrockTeleport);
                if (teleport == null) {
                    // sad face
                    this.currentState = State.REVIVED;
                } else {
                    if (teleport.interact("break")) {
                        this.currentState = State.ATVARROCK;
                    }
                }
            }
        } else if (this.currentState == State.ATVARROCK) {
            boolean atGrandeExchangio = Banks.GRAND_EXCHANGE.contains(myPosition());
            super.log("atGrandeExchangio: " + atGrandeExchangio);
            if (!atGrandeExchangio) {
                boolean walkingTowardsGE = getWalking().webWalk(getLocationArea);
                super.log("walkingTowardsGE: " + walkingTowardsGE);
                if (walkingTowardsGE) {
                    this.currentState = State.TRAVELINGTOGRANDEXCHANGE;
                }
            }
        } else if (this.currentState == State.TRAVELINGTOGRANDEXCHANGE) {
            super.log("entered Traveling to GE Block");
            if (getSettings().getRunEnergy() >= 10) {
                getSettings().setRunning(true);
            }

            boolean atGrandeExchangio = Banks.GRAND_EXCHANGE.contains(myPosition());
            super.log("atGrandeExchangio2: " + atGrandeExchangio);
            Position pos = new Position(3161, 3502, 0);
            getMouse().click(new MainScreenTileDestination(getBot(), pos));
            Thread.sleep(1000);
            if (atGrandeExchangio) {
                this.currentState = State.WAITFORPLAYERDEATH;
            }
        } else if (this.currentState == State.WAITFORPLAYERDEATH) {
            super.log("entered Wait for Player death block");
            Position pos1 = new Position(3164, 3474, 0);
            WalkingEvent event = new WalkingEvent(pos1);
            event.setMinDistanceThreshold(0);
            execute(event);
            for (Player player : getPlayers().getAll()) {
                Thread.sleep(100);
                super.log("examining player: " + player.getName());
                //filter players to meet lootable
                if (player != null && player.isAnimating()
                        && player.isHitBarVisible() && player.isOnScreen()) {
                    //get player health
                    int playerhealth = player.getHealthPercent();
                    //ID dead player who just died and get their name, coordinates, and health.
                    if (playerhealth == 0) {
                        String name = player.getName();
                        String loc = player.getPosition().toString();
                        //log all as a test
                        super.log("found dead player" + name + loc + playerhealth);
                        getWalking().walk(new Position(player.getPosition()));
                        this.DrawTile = player.getPosition();
                        this.currentState = State.WAITINGFORLOOT;
                    }
                }
            }
        } else if (this.currentState == State.WAITINGFORLOOT) {
            //using this state to test
            log("Waiting for looteio!");
            for (GroundItem itemonground : groundItems.getAll()) {
                if (itemonground != null && itemonground.exists() && itemonground.getGridX() == myPlayer().getGridX()
                        && itemonground.getGridY() == myPlayer().getGridY()) {
                    itemonground.interact("Take");
                    this.currentState = State.CHECKINGLOOT;
                }
            }
        } else if (this.currentState == State.CHECKINGLOOT) {
            int totalInventoryValue = 0;
            for (Item item : getInventory().getItems()){
                if(item != null)
                    totalInventoryValue += getPrice(item.getName()) * item.getAmount();
            }
            log("the total loot  is  " + (totalInventoryValue));
            int openslots = (getInventory().getEmptySlotCount());
            log("you have " + (28 - openslots) + " items");
            if (totalInventoryValue >= 6000 || openslots <= 10) {
                log("Value high, or inv full banking");
                getWalking().webWalk(getLocationArea);
                getBank().open();
                getBank().depositAll();
                getBank().close();

        } else {
                this.currentState = State.WAITFORPLAYERDEATH;
        }
        }
        }

    private Integer getPrice(String itemName) {
        Optional<String> itemPrice = ItemLookup.get(itemName, Property.SELL_AVERAGE);
        return itemPrice.map(Integer::valueOf).orElse(0);
    }



    @Override
    public void onStart() {
        this.currentState = State.WAITINGFORLOOT;
    }
    @Override
    public void onExit () {

    }


    @Override
    public int onLoop () {


           try {
                if (myPlayer().isVisible()) {
                    super.log("about to loop!");
                    this.bank();
                }
            } catch (InterruptedException e) {
                log("got interrupted!");
            }



            return random(10, 30);

        }


    @Override
    public void onPaint (Graphics2D g) {
        Polygon p = this.DrawTile.getPolygon(bot);
        g.drawPolygon(p);

        }
      }





