package LOOT;

import org.osbot.rs07.api.Players;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Player;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.api.model.*;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

import java.awt.*;
import java.io.IOException;
import java.util.Random;

@ScriptManifest(name = "GE LOOTER", author = "Pat and Mac", version = 1.0, info = "Goes to GE and Loots from Dead players", logo = "")
public class Main extends Script {
    private State currentState = State.REVIVED;
    public static Area getLocationArea = Banks.GRAND_EXCHANGE;
    private final Area bankingArea = new Area(3225, 3212, 3218, 3225);
    private final Area lootingArea = new Area(3162, 3486, 3167, 3481);
    private Position DrawTile = new Position(1, 2, 3);
    private final String varrockTeleport = "Varrock teleport";
    private int PriceID;
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
            super.log("Executing Revived");

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
                boolean walkingTowardsGE = getWalking().webWalk(new Position[]{getLocationArea.getRandomPosition()});
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
            if(atGrandeExchangio){
              this.currentState = State.WAITFORPLAYERDEATH;
            }
        } else if (this.currentState == State.WAITFORPLAYERDEATH) {
            super.log("entered Wait for Player death block");
            //get a list of all players because there are some
            java.util.List<Player> players = getPlayers().getAll();
            //check to see if players exist
            if (players != null) {
                for(Player player : players) {
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
            }
        } else if (this.currentState == State.WAITINGFORLOOT) {
          super.log("Waiting for looteio!");
        }

        return;
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onExit () {

    }

    public java.util.List<GroundItem> getAll;
    public String[] temp;
    @Override
    public int onLoop () {
        /*
        //Random
        Random rand = new Random();
        //check to see if players exist
        if (getPlayers().getAll() != null) {
            //get a list of all players because there are some
            java.util.List<Player> p = getPlayers().getAll();
            //get player size and select random one
            int randomNum = rand.nextInt(((p.size() - 1) - 0) + 1) + 0;
            //select the player
            Player randomPlayer = p.get(randomNum);
            //filter players to meet lootable
            if (randomPlayer != null && randomPlayer.isAnimating() && randomPlayer.isHitBarVisible() && randomPlayer.isOnScreen()) {
                //get player health
                int playerhealth = randomPlayer.getHealthPercent();
                //ID dead player who just died and get their name, coordinates, and health.
                if (playerhealth == 0) {
                    String name = randomPlayer.getName();
                    String loc = randomPlayer.getPosition().toString();
                    //log all as a test
                    log(name + loc + playerhealth);

                    getWalking().walk(new Position(randomPlayer.getPosition()));
                    DrawTile = randomPlayer.getPosition();
                    try {
                        // to sleep 10 seconds
                        Thread.sleep(58000);
                    } catch (InterruptedException e) {
                        // recommended because catching InterruptedException clears interrupt flag
                        Thread.currentThread().interrupt();
                        // you probably want to quit if the thread is interrupted
                    }

                    // NULL POINTER
                    temp = getAll.toString().split("/n");
                    for (int i = 0; i < temp.length; i++) {
                        if (groundItems.closest(temp[i]) != null) {
                            GroundItem loot = groundItems.closest(temp[i]);
                            loot.interact("Take");
                        }
                    }
                }
            }
        }
        */

        try {
            if (myPlayer().isVisible()) {
              super.log("about to loop!");
              this.bank();
            }
        } catch(InterruptedException e) {
            log("got interrupted!");
        }

        return random(10, 30);
    }

    @Override
    public void onPaint (Graphics2D g){
        Polygon p = this.DrawTile.getPolygon(bot);
        g.drawPolygon(p);
    }
}
