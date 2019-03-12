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
import org.osbot.rs07.script.Script;
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
        /* state 1: revived from death */
        if(this.currentState == State.REVIVED) {
            // if with walking distance of bank, walk to bank
            if ((!bankingArea.contains(myPosition())) && (!myPlayer().isMoving())) {
                if (super.getWalking().walk(bankingArea)) {
                    this.currentState = State.TRAVELINGTOBANKCHEST;
                }
            }
        } else if(this.currentState == State.TRAVELINGTOBANKCHEST) {
            this.whileTravelingToBank.sleep();

            if(bankingArea.contains(myPosition())) {
                this.currentState = State.ATBANKCHEST;
            }
        } else if(this.currentState == State.ATBANKCHEST) {
            if (!getBank().isOpen()) {
                if (getBank().open()) {
                    this.currentState = State.ATBANKPROMPT;
                    //doesnt meet reqs
                }
            }
        }

        /* END STATE 2 */


        if (!inventory.contains(new String[]{varrockTeleport}))
            if (getBank().contains(new String[]{varrockTeleport})) {
                getBank().withdraw(varrockTeleport, 1);
            }


        if (inventory.contains(new String[]{varrockTeleport})) {
            getBank().close();
            inventory.getItem(varrockTeleport).interact("break");
            if (!inventory.contains(new String[]{varrockTeleport})) {


                if (!lootingArea.contains(myPlayer())) {
                    if (getSettings().getRunEnergy() >= 10) {
                        getSettings().setRunning(true);
                    }


                    getWalking().webWalk(new Area[]{lootingArea});
                } else {
                    stop();
                }

            }
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
                this.bank();
            }
        } catch(InterruptedException e) {
            log("got interrupted!");
        }


        return random(100, 300);
    }


    @Override
    public void onPaint (Graphics2D g){


    }
}


