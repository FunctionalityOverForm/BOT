package LOOT;

/* To Do:

1. Handle Location traveling (include option to teleport using tabs?(
if != location grand exchange, go clostest bank, find varrock teleport, close interface, and click tab. Move to G.E.)

bank chest lumby name: Bank chest
object id: 7411
interact option: use


teleport info
name: Varrock teleport
ID: 12325



public static final Area GEArea = new Area(3144, 3512, 3182, 3472);
public static final Area innerGEArea = new Area(3161, 3493, 3168, 3486);



2.Identify players who may die, organize priority of players.
16 tiles from my player is the area i can read from

get list of players, focus on one fight (closest, or something)
get those two players names
store them as variables

player1
player2



3.If player dies go to their death square.

836 - death animation
maybe just wait for closest death animation, get cords of player doing animation, walk to that location.


if (player1 != null && player1.exists() && player1.getHealth() > 0);
log(player1 && "just died")
||
if (player2 != null && player2.exists() && player2.getHealth() > 0);
log(player2 && "just died")

go to player
start timer until loot appears (55 seconds to be safe)


switch state "LOOT"


4.Wait for loot to appear fro

5.Spam click ground to pick up loot

6. if value > 30k of inventory go to bank


 GrandExchange parser = new GrandExchange();


        try {
            int price = parser.getBuyingPrice(1079);
           String hi = String.valueOf(price);
            log(hi);
        } catch (java.io.IOException e){
            e.printStackTrace();
            log("not work");

        }


7. repeat

 */





import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.Players;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Player;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.api.model.*;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

import java.awt.*;




@ScriptManifest(name = "GE LOOTER", author = "Pat and Mac", version = 1.0, info = "Goes to GE and Loots from Dead players", logo = "")
public class Main extends Script {
    private State currentState = State.REVIVED;

    private final Area bankingArea = new Area(3225, 3212, 3218, 3225);
    private final Area lootingArea = new Area(3162, 3486, 3167, 3481);
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
                boolean walkingTowardsGE = super.getWalking().walk(Banks.GRAND_EXCHANGE.getRandomPosition());
                super.log("walkingTowardsGE: " + walkingTowardsGE);
                if (walkingTowardsGE) {
                    this.currentState = State.TRAVELINGTOGRANDEXCHANGE;
                }
            }
        } else if (this.currentState == State.TRAVELINGTOGRANDEXCHANGE) {
            if (!lootingArea.contains(myPlayer())) {
                if (getSettings().getRunEnergy() >= 10) {
                    getSettings().setRunning(true);
                }
            } else {
                this.currentState = State.ATGRANDEXCHANGE;
            }
        } else if (this.currentState == State.ATGRANDEXCHANGE){
            super.log("I'm herrreeeeee!!!!");
        } else {
            super.log("else????? currentState: " + this.currentState);
        }
    }




    @Override
    public void onStart() {

    }



    @Override
    public void onExit () {


    }


    @Override
    public int onLoop () {
        try {
            if (myPlayer().isVisible()) {
                super.log("in is visible block");
                this.bank();
            }
        } catch(InterruptedException e) {
            log("got interrupted!");
        }


        return random(500, 800);
    }


    @Override
    public void onPaint (Graphics2D g){


    }
}


