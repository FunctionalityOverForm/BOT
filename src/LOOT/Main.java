package LOOT;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Player;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.input.mouse.MainScreenTileDestination;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.api.model.*;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import java.awt.*;
import java.util.Optional;
import java.util.Random;


@ScriptManifest(name = "GE LOOTER", author = "Pat", version = 1.0, info = "Goes to GE and Loots from Dead players", logo = "")

public class Main extends Script {
    public long starttime = 0;
    public int Deaths = 0;
    public long totalvalue = 0;
    private State currentState = State.REVIVED;
    public static Area getLocationArea = Banks.GRAND_EXCHANGE;
    private final Area bankingArea = new Area(3225, 3212, 3218, 3225);
    private Position DrawTile = new Position(1, 2, 3);
    private final String varrockTeleport = "Varrock teleport";
    private Random randomforarray = new Random();
    private String[] Chattingoptions = {"Dang, gg", "lol nice", "leave some for me", "wtf...", "lucky kill lol", "he wants rm"};
    int randomIndex = randomforarray.nextInt(Chattingoptions.length);
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
            //state working
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
            //state working
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
            //state working
        } else if (this.currentState == State.ATVARROCK) {
            boolean atGrandeExchangio = Banks.GRAND_EXCHANGE.contains(myPosition());
            super.log("atGrandeExchangio: " + atGrandeExchangio);
            if (myPlayer().getHealthPercent() == 0) {currentState = State.REVIVED;Deaths++;}
            if (!atGrandeExchangio) {
                boolean walkingTowardsGE = getWalking().webWalk(getLocationArea);
                super.log("walkingTowardsGE: " + walkingTowardsGE);
                if (walkingTowardsGE) {
                    this.currentState = State.TRAVELINGTOGRANDEXCHANGE;
                }
            }
            //state working
        } else if (this.currentState == State.TRAVELINGTOGRANDEXCHANGE) {
            super.log("entered Traveling to GE Block");
            if (myPlayer().getHealthPercent() == 0) {currentState = State.REVIVED;Deaths++;}
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
            //state working
        } else if (this.currentState == State.WAITFORPLAYERDEATH) {
            super.log("entered Wait for Player death block");
            Position pos1 = new Position(3180, 3486, 0);
            WalkingEvent event = new WalkingEvent(pos1);
            event.setMinDistanceThreshold(0);
            execute(event);
            for (Player player : getPlayers().getAll()) {
                Thread.sleep(10);
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
                        Thread.sleep(600);
                        getWalking().walk(new Position(player.getPosition()));
                        Thread.sleep(600);
                        getWalking().walk(new Position(player.getPosition()));
                        this.DrawTile = player.getPosition();
                        String typing = Chattingoptions[randomIndex];
                        getKeyboard().typeString(typing);
                        this.currentState = State.WAITINGFORLOOT;
                        if (myPlayer().getHealthPercent() == 0) {currentState = State.REVIVED;Deaths++;}
                    }
                }
            }
//state is working, needs a timeout in case mis click, or loot never appears
        } else if (this.currentState == State.WAITINGFORLOOT) {
            log("Waiting for looteio!");
            if (myPlayer().getHealthPercent() == 0) {currentState = State.REVIVED; Deaths++; }
            for (GroundItem itemonground : groundItems.getAll()) {
                if (itemonground != null && itemonground.exists()
                        && itemonground.getGridX() == myPlayer().getGridX()
                        && itemonground.getGridY() == myPlayer().getGridY()) {
                    itemonground.interact("Take");
                    Thread.sleep(650);
                    currentState = State.CHECKINGLOOT;
                }
            }
//state working
        } else if (this.currentState == State.CHECKINGLOOT) {
            if (randomIndex == 5) {randomIndex = 0;}
            else if (randomIndex < 5) {randomIndex++;}
            int totalInventoryValue = 0;

            for (Item item : getInventory().getItems()) {
                if (item != null)
                    totalInventoryValue += getPrice(item.getName()) * item.getAmount();
            }
            log("the total loot  is  " + (totalInventoryValue));
            totalvalue = totalInventoryValue;
            int openslots = (getInventory().getEmptySlotCount());
            log("you have " + (28 - openslots) + " items");
            if (myPlayer().getHealthPercent() == 0) {currentState = State.REVIVED;Deaths++;}
            if (totalInventoryValue >= 6000 || openslots <= 10) {
                log("Value high, or inv full banking");
                getWalking().webWalk(getLocationArea);
                getBank().open();
                Thread.sleep(600);
                getBank().depositAll();
                Thread.sleep(600);
                getBank().close();
                this.currentState = State.WAITFORPLAYERDEATH;
            } else {this.currentState = State.WAITFORPLAYERDEATH;}
        }
    }
    private Integer getPrice(String itemName) {
        Optional<String> itemPrice = ItemLookup.get(itemName, Property.SELL_AVERAGE);
        return itemPrice.map(Integer::valueOf).orElse(0);}


    @Override
    public void onStart() {
if(getWorlds().getCurrentWorld() != 325) {
    log("hopping...");
    getWorlds().hop(325);
    getDialogues().dialogues.selectOption("Switch to the PvP world.");
    if(getWorlds().getCurrentWorld() == 325) {
       log("hopped to pvp world"); }

}
//this.currentState = State.WAITFORPLAYERDEATH;
starttime = System.currentTimeMillis();
    }

    @Override
    public void onExit() {

    }


    @Override
    public int onLoop() {
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
    public void onPaint(Graphics2D g) {
        g.setColor(Color.decode("#cccccc"));
        g.fillRect(5,345, 512, 130);
        g.drawRect(5, 345, 512, 130);
        //
        g.setColor(Color.decode("#393838"));
        g.fillRect(5,345, 512, 30);
        g.drawRect(5, 345, 512, 30);
        //
        g.setColor(Color.decode("#535353"));
        g.fillRect(5,345, 512, 12);
        g.drawRect(5, 345, 512, 12);
        //
        g.setColor(Color.decode("#cccccc"));
        Font font = new Font("Ariel", Font.PLAIN, 18);
        g.setFont(font);
        g.drawString("GRAND EXCHANGE LOOTER", 8, 367);
        //
        g.fillOval(503,353, 4, 4);
        g.drawOval(500,350, 10, 10);
        //
        g.setColor(Color.decode("#393838"));
        Font font2 = new Font("Ariel", Font.PLAIN, 14);
        g.setFont(font2);
        long runTime = (System.currentTimeMillis()  - starttime) / 1000;
        String secondstext = " seconds";
        String timerunning = "Time running: ";
        String Status = "Starting Script";
        if(currentState == State.REVIVED){ Status = "At Lumbridge, going to bank for teleport.";}
        else if( currentState == State.ATBANKPROMPT ){ Status = "Getting Teleport to Varrock.";}
        else if( currentState == State.ATVARROCK ){ Status = "At Varrock, Walking to Grand Exchange.";}
        else if( currentState == State.ATGRANDEXCHANGE ){ Status = "Arrived, walking to looting area";}
        else if( currentState == State.WAITFORPLAYERDEATH ){ Status = "Waiting for player to die.";}
        else if( currentState == State.WAITINGFORLOOT ){ Status = "Getting Loot.";}
        else if( currentState == State.CHECKINGLOOT ){ Status = "Checking Value..Checking to see if banking is needed.";}
        g.drawString(timerunning + String.valueOf(runTime)+secondstext  , 10, 395);
        g.drawString("Status: " +Status  , 10, 415);
        g.drawString("Last Loot's Value: " +totalvalue , 10, 435);
        g.drawString("Phrase: " + Chattingoptions[randomIndex], 10, 452);
        g.drawString("Deaths: " + Deaths, 10, 469);
        //
        g.setColor(Color.green);
        Polygon p = this.DrawTile.getPolygon(bot);
        g.drawPolygon(p);


    }
}







