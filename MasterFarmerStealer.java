/**
 * Author: Brian Konzman
 * Date: 3/23/2015
 */
    import java.awt.Color;
    import java.awt.Font;
    import java.awt.Graphics;
    import java.awt.Graphics2D;
    import java.awt.Point;
    import java.awt.event.ActionEvent;
    import java.awt.event.ActionListener;
    import java.awt.event.MouseEvent;
    import java.awt.event.MouseListener;

    import java.io.BufferedReader;
    import java.io.File;
    import java.io.FileInputStream;
    import java.io.FileOutputStream;
    import java.io.IOException;
    import java.io.InputStreamReader;

    import java.net.URL;
    import java.net.URLConnection;

    import java.util.Properties;

    import javax.swing.JButton;
    import javax.swing.JCheckBox;
    import javax.swing.JFrame;
    import javax.swing.JLabel;
    import javax.swing.JSlider;
    import javax.swing.JTextField;
    import javax.swing.SwingUtilities;


    import org.rsbot.event.events.MessageEvent;
    import org.rsbot.event.listeners.MessageListener;
    import org.rsbot.event.listeners.PaintListener;
    import org.rsbot.script.Script;
    import org.rsbot.script.ScriptManifest;
    import org.rsbot.script.methods.Bank;
    import org.rsbot.script.methods.Equipment;
    import org.rsbot.script.methods.Game.Tab;
    import org.rsbot.script.methods.Skills;
    import org.rsbot.script.util.Filter;
    import org.rsbot.script.util.Timer;
    import org.rsbot.script.wrappers.RSPlayer;
    import org.rsbot.script.wrappers.RSCharacter;
    import org.rsbot.script.wrappers.RSTile;
    import org.rsbot.script.wrappers.RSObject;
    import org.rsbot.script.wrappers.RSNPC;
    import org.rsbot.script.wrappers.RSArea;
    import org.rsbot.script.wrappers.RSItem;
    import org.rsbot.script.wrappers.RSComponent;


    @ScriptManifest(name = "konzy's Master Stealer", authors = "konzy", keywords = "Thieving",
            version = 1.17, description = "Master Farmer Thieving by konzy")

    public class MasterFarmerStealer extends Script implements PaintListener, MessageListener , MouseListener
    {

        final ScriptManifest properties = getClass().getAnnotation(ScriptManifest.class);

        static final int GUARD_ID[] = { 2236 , 8435 };
        static final int BOOTH_ID = 2012;
        static final int FARMER_ID = 2234;
        static final int GLOVES_OF_SILENCE = 10075;
        static final int GLOVES_REQ_LVL = 54;
        static final int TILE_RANGE = 3;

        static final RSArea MARKET_AREA = new RSArea(new RSTile(3069, 3245), new RSTile(3086, 3258));
        static final RSArea BANK_AREA = new RSArea(new RSTile(3092,3240),new RSTile(3097,3246));
        static final RSArea RESPAWN_AREA = new RSArea(new RSTile(3217,3213),new RSTile(3230,3224));
        static final RSArea PATH_AREA = new RSArea(new RSTile(3087,3247), new RSTile(3094,3250));
        static final RSTile BANK_TILE = new RSTile(3093,3244);
        static final RSTile EXIT_TILE = new RSTile(3092,3248);
        static final RSTile CENTRAL_TILE = new RSTile(3088,3248);
        static final RSTile CASTLE_EXIT = new RSTile(3234,3220);


        static final RSTile DEATHWALK_PATH[] = {
                CASTLE_EXIT, new RSTile(3229,3232), new RSTile(3222,3241), new RSTile(3104,3247), new RSTile(3070, 3250)
        };

        static final RSTile OUT_OF_CASTLE[] = {
                new RSTile(3225,3219), new RSTile(3229,3219), new RSTile(3234,3220)
        };

        static final int[] KEEP_IDS = {
                5295, 5296, 5298, 5299, 5300, 5301, 5302, 5303, 5304,
                5321
        };

        static final String KEEP_NAMES[] = {
                "ranarr", "toadflax", "avantoe", "kwuarm",
                "snapdragon", "cadantine", "lantadyme", "dwarf", "torstol",
                "watermelon"
        };

        static final int[] DONT_DROP_ID = {
                617, 1733, 1734, 10115, 10076, GLOVES_OF_SILENCE, 15527, 15526
        };

        static final int[] DROP_ID = {
                5319, 5307, 5305, 5322, 5099, 5310, 5308, 5102, 5294, 5309,
                5101, 5096, 5324, 5306, 5291, 5103, 5292, 5097, 5281, 5098,
                5105, 5106, 5280, 5297, 5311, 5104, 5293, 5318, 5282, 1993,
                5320
        };

        static final int[] MISC_DROP_ID = {
                1937, 1993, 1935, 1937, 1987, 7919, 14665,6961, 2011, 1985,
                1901, 2327, 6963
        };

        private static final int[] ALL_SEED_IDS = {
                5096, 5097, 5098, 5099, 5100, 5101, 5102, 5103, 5104, 5105,
                5106, 5280, 5281, 5282, 5291, 5292, 5293, 5294, 5295, 5296,
                5297, 5298, 5299, 5300, 5301, 5302, 5303, 5304, 5305, 5306,
                5307, 5308, 5309, 5310, 5311, 5318, 5319, 5320, 5321, 5322,
                5323, 5324
        };

        private static final String[] ALL_SEED_NAMES = {
                "marigold", "rosemary", "nasturtium", "woad", "limpwurt",
                "redberry", "cadavaberry", "dwellberry", "jangerberry",
                "whiteberry", "poison", "cactus", "belladonna",
                "mushroom", "guam", "marrentill", "tarromin",
                "harralander", "ranarr", "toadflax", "irit", "avantoe",
                "kwuarm", "snapdragon", "cadantine", "lantadyme", "dwarf",
                "torstol", "barley", "jute", "hammerstone", "asgarnian",
                "yanillian", "krandorian", "wildblood", "potato", "onion",
                "sweetcorn", "watermelon", "tomato", "strawberry", "cabbage"
        };

        private boolean showPaint, guiWait, failedPick, useBanking, dropSeeds,
                canWearGloves, screenshotOnExit, adjustFood, keepHighSeeds, seedsAreInBank,
                glovesAreInBank, highSeedsAreFull, equipNewGloves, priceDone, foodDone, reeval,
                foodStarted;

        private boolean threadRun = true;
        private boolean useFood = true;
        private boolean outOfDate = false;

        private double currentVersion;
        private int  HP, startLvl, gainedLvls, profit, foodPrice,
                clickDelay, mouseDelay, succeedPickpockets, failPickpockets,
                foodEaten, glovePrice, foodID, glovesUsed, glovesToKeep;
        private int handID = GLOVES_OF_SILENCE;
        private int bailHP = 40;
        private int withdrawFood = 5;
        private int minGoldToKeep = 50;
        private long startXP, startTime, lastMoveTime;
        private RSTile lastLocation;
        private RSItem carriedFood;
        private RSNPC masterFarmer;
        private String lastMsg = "";
        private String status = "Starting Up";
        private Seed[] seedArray = new Seed[ALL_SEED_NAMES.length];
        private Seed[] highSeedArray = new Seed[KEEP_NAMES.length];
        private konzyMasterStealerGUI gui;

        private final CameraHandler cameraHandler = new CameraHandler();

        private final Filter<RSNPC> GUARD_FILTER = new Filter<RSNPC>() {
            @Override
            public boolean accept(RSNPC n) {
                RSCharacter c = n.getInteracting();
                return c != null &&
                        c instanceof RSPlayer &&
                        c.equals(getMyPlayer()) &&
                        (n.getID() == GUARD_ID[0] || n.getID() == GUARD_ID[1]);
            }
        };


        enum STATE {

            WALK_TO_BANK,	//just walk to bank
            FAILED_PICK,	//When stealing goes wrong
            DROP_SEEDS,		//Drop Seeds
            EAT_FOOD,		//Eat food
            EQUIP_GLOVES,	//Equip gloves of silence
            DEPOSIT,		//Deposit Inventory
            WITHDRAW_GLOVES,//Withdraw gloves from bank
            WITHDRAW_SEEDS,	//Withdraw seeds from bank
            WITHDRAW_FOOD,	//Withdraw food from bank
            WALK_TO_MARKET,	//just walks
            STEAL,			//Steals from master farmer
            RANDOM,			//Detects if in a random
        }

        private STATE getEnum(){
            try {
                if(game.inRandom()
                        || (interfaces.getComponent(906,231).isValid() &&
                        interfaces.getComponent(906,236).isValid() &&
                        interfaces.getComponent(906,236).containsText("Back") &&
                        interfaces.getComponent(906,231).containsAction("Back"))
                        || (interfaces.getComponent(906,160).isValid() &&
                        interfaces.getComponent(906,173).isValid() &&
                        interfaces.getComponent(906,173).containsText("Play") &&
                        interfaces.getComponent(906,160).containsAction("Play"))
                        || game.isLoginScreen() || game.isWelcomeScreen()){
                    status = "Random";
                    return STATE.RANDOM;
                }
            } catch (Exception e) {}

            if (gettingAttacked()){
                status = "RUN AWAY!!!";
                walking.setRun(true);
                return STATE.WALK_TO_BANK;
            }
            if(failedPick){
                status = "failed";
                return STATE.FAILED_PICK;
            }
            if (inventory.getCount(GLOVES_OF_SILENCE) > 0 &&
                    (equipNewGloves || handID != GLOVES_OF_SILENCE)){
                status = "Equipping Gloves";
                return STATE.EQUIP_GLOVES;
            }
            if(inventory.isFull()){
                if(dropSeeds){
                    status = "Dropping Seeds";
                    return STATE.DROP_SEEDS;
                }
                if(inventory.contains(foodID) && adjustFood && withdrawFood > 1)
                    withdrawFood--;
            }
            if(bank.isOpen()){
                if(((inventory.getCount(GLOVES_OF_SILENCE) < glovesToKeep) ||
                        (handID != GLOVES_OF_SILENCE && inventory.getCount(GLOVES_OF_SILENCE) == 0)) &&
                        canWearGloves && glovesAreInBank){
                    status = "Getting Gloves";
                    return STATE.WITHDRAW_GLOVES;
                }else if(!highSeedsAreFull &&
                        keepHighSeeds &&
                        seedsAreInBank){
                    status = "Getting Seeds";
                    return STATE.WITHDRAW_SEEDS;
                }else{
                    status = "Getting Food";
                    return STATE.WITHDRAW_FOOD;
                }
            }
            if((((carriedFood == null && foodID == 0 && useFood) ||
                    (!inventory.contains(foodID) && useFood)  ||
                    inventory.isFull() && !keepHighSeeds) ||
                    ((canWearGloves && handID != GLOVES_OF_SILENCE && glovesAreInBank) || //If we can wear gloves, not wearing, not in inventory, and have some in bank
                            (keepHighSeeds && !highSeedsAreFull && seedsAreInBank))) &&
                    useBanking){
                status = "Depositing Inventory";
                return STATE.DEPOSIT;
            }
            if (!MARKET_AREA.contains(getMyPlayer().getLocation())){
                status = "Walking Back To Market";
                return STATE.WALK_TO_MARKET;
            }
            return STATE.STEAL;
        }

        public boolean onStart() {

            try {
                URLConnection url = new URL("http://scripters.powerbot.org/files/273499/konzyMasterStealerVERSION.txt").openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(url.getInputStream()));
                currentVersion = Double.parseDouble(in.readLine());
                if (currentVersion > properties.version()) {
                    outOfDate = true;
                    log(new Color(0, 204, 0), "Please Update konzy's Master Stealer");
                } else
                    log("You have the latest version of konzy's");
                if (in != null)
                    in.close();
            } catch (IOException e) {
                log("Problem getting version :/");
                return false;
            }

            if(!game.isLoggedIn()) {
                log("Start logged in.");
                return false;
            }
            for(int i = 0; i < ALL_SEED_NAMES.length ;i++)
                seedArray[i] = new Seed(ALL_SEED_NAMES[i] , ALL_SEED_IDS[i]);

            for(int i = 0; i < KEEP_NAMES.length ;i++)
                highSeedArray[i] = new Seed(KEEP_NAMES[i] , KEEP_IDS[i]);

            new PriceThread().start();
            guiWait = true;
            try {
                carriedFood = getFood();
                foodID = carriedFood.getID();
            } catch (Exception e) {}
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        gui = new  konzyMasterStealerGUI();
                    }
                });
            } catch (Throwable e) {
            }
            gui.setVisible(true);
            while (gui.isVisible())
                sleep(100);
            gui.dispose();
            if(guiWait)
                return false;
            closeWindows();
            lastLocation = getMyPlayer().getLocation();
            if (skills.getRealLevel(Skills.HUNTER) >= GLOVES_REQ_LVL){
                canWearGloves = true;
                handID = equipment.getItem(Equipment.HANDS).getID();
                if(glovesToKeep == 0)
                    glovesToKeep = 1;
                if(!keepHighSeeds)
                    glovesToKeep = 0;

            }else
                glovesToKeep = 0;

            if(withdrawFood == 0)
                useFood = false;



            if(keepHighSeeds){
                withdrawFood = 28 - (glovesToKeep + KEEP_IDS.length);
                useFood = true;
                adjustFood = false;
                dropSeeds = false;
            }
            if(glovesToKeep >= 18){
                glovesToKeep = 18;
                useFood = false;
                withdrawFood = 0;
            }

            if ((withdrawFood > 25 || withdrawFood < 0) && useBanking){
                log("You can only withdraw food between 1 and 25");
                return false;
            }

            try {
                carriedFood = getFood();
                foodID = carriedFood.getID();
            } catch (Exception e) {}

            if(foodID == 0 && carriedFood == null && useBanking && useFood){
                log("Either start with food in your inventory or enter Food Override");
                return false;
            }

            camera.setPitch(true);
            sleep(2000);
            startTime = (int)System.currentTimeMillis();
            startXP = skills.getCurrentExp(Skills.THIEVING);
            startLvl = skills.getCurrentLevel(Skills.THIEVING);
            showPaint = true;
            cameraHandler.start();
            return true;
        }


        private void closeWindows(){
            if(store.isOpen()){
                store.close();
                int i = 0;
                while(store.isOpen() && i < 50){
                    sleep(100);
                    i++;
                }
            }
            if(bank.isOpen()){
                bank.close();
                int i = 0;
                while(bank.isOpen() && i < 50){
                    sleep(100);
                    i++;
                }

            }
            if (interfaces.canContinue()){
                interfaces.clickContinue();

                int i = 0;
                while(interfaces.getContinueComponent().isValid() && i < 50){
                    sleep(100);
                    i++;
                }
            }


        }

        private RSItem getFood(){
            for(RSItem i : inventory.getItems()) {
                if(i == null || i.getID() == -1)
                    continue;
                if (i.getComponent().getActions() == null || i.getComponent().getActions()[0] == null)
                    continue;
                if (i.getComponent().getActions()[0].contains("Eat")){
                    return i;
                }
            }
            return null;
        }

        private int getCurrentLifepoint() {
            if (interfaces.get(748).getComponent(8).getText() != null && interfaces.get(748).getComponent(8).isValid()) {
                HP = Integer.parseInt(interfaces.get(748).getComponent(8).getText());
            }
            if (HP > 0)
                return HP;
            return 1000;
        }

        public boolean isHealthLow() {
            int CurrHP = getCurrentLifepoint() / 10;
            int RealHP = skills.getRealLevel(Skills.CONSTITUTION);
            if (CurrHP <= random(RealHP / 2, RealHP / 1.5)) {
                return true;
            }
            return false;
        }

        public boolean gettingAttacked(){
            RSNPC[] mGuards = npcs.getAll(GUARD_FILTER);
            for (RSNPC mGuard : mGuards) {
                RSCharacter interacting = mGuard.getInteracting();
                if (interacting == null) {
                    continue;
                }
                if (interacting.equals(getMyPlayer())) {
                    return true;
                }
            }
            return false;
        }

        private boolean eatFood() {
            carriedFood = getFood();
            for(int i = 0; i < 3; i++) {
                if(!inventory.contains(foodID)){
                    log("cant find food");
                    break;
                }

                try{
                    if(carriedFood.getComponent().containsText("Drink")){
                        if(carriedFood.interact("Eat")) {
                            status = "Eating Food";
                            while(isHealthLow())
                                sleep(100);
                            profit -= foodPrice;
                            return true;
                        }
                    }else if(carriedFood.interact("Eat")) {
                        status = "Eating Food";
                        while(isHealthLow())
                            sleep(100);
                        profit -= foodPrice;
                        return true;
                    }
                }catch(Exception e){}
            }
            if (getCurrentLifepoint() <= bailHP) {
                log.warning("HP less than" + bailHP + "Logging out.");
                game.logout(true);
                stopScript(true);
            }
            return false;
        }

        public boolean openBank() {
            status = "Opening Bank";
            try {
                final RSObject bankBooth = objects.getNearest(Bank.BANK_BOOTHS);
                if (objects.getNearest(Bank.BANK_BOOTHS).isOnScreen() && !bank.isOpen() && bankBooth != null){
                    bankBooth.interact("Use-Quickly");
                    int i = 0;
                    while(!bank.isOpen() && i < 20){
                        sleep(250);
                        i++;
                    }
                }
            } catch (Exception e) {}
            return bank.isOpen();
        }

        public void dropJunkSeeds(){
            if(dropSeeds){
                int dontDrop[] = new int[150];
                int i = 0;

                for(Seed s: seedArray){
                    if(s.getPrice() >= minGoldToKeep || s.getPrice() < 0){
                        dontDrop[i] = s.getID();
                        i++;
                    }
                }
                for(int j:DONT_DROP_ID){
                    dontDrop[i] = j;
                    i++;
                }
                dontDrop[i + 1] = foodID;
                while (inventory.getCountExcept(dontDrop) > 0)
                    inventory.dropAllExcept(dontDrop);
            }
        }


        public void onFinish() {
            threadRun = false;
            log("Last Food Withdraw was " + withdrawFood +
                    " Gained " + (skills.getCurrentLevel(Skills.THIEVING) - startLvl) + "Levels, and Profited " +
                    profit);
            if (screenshotOnExit) {
                env.saveScreenshot(true);
                log("Screenshot taken!");
                sleep(500);
            }
        }

        class Seed {

            final String name;
            final int id;
            int price = -1;
            int procured = 0;

            public Seed(String name, int id) {
                this.name = name;
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public int getPrice() {
                return price;
            }

            public int getID() {
                return id;
            }

            public int getIncrement() {
                return procured;
            }

            public void setIncrement(int procured) {
                this.procured += procured;
            }

            public void setPrice() {
                this.price = grandExchange.lookup(id).getGuidePrice();
            }
        }

        private class PriceThread extends Thread {
            public void run() {
                glovePrice = grandExchange.lookup(GLOVES_OF_SILENCE).getGuidePrice();
                for (Seed s : seedArray) {
                    s.setPrice();
                }
                boolean wait = true;
                while(wait){
                    wait = false;
                    for(Seed s:seedArray){
                        if(s.getPrice() < 0)
                            wait = true;
                        if(glovePrice == 0)
                            wait = true;
                    }
                }

                priceDone = true;
            }
        }

        private class FoodThread extends Thread {
            @Override
            public void run() {
                foodStarted = true;
                foodPrice = grandExchange.lookup(foodID).getGuidePrice();
                foodDone = true;
            }
        }

        private void reevaluateProfit(){
            profit = ((foodEaten * foodPrice) + (glovesUsed * glovePrice));
            for(Seed s: seedArray)
                profit += (s.getPrice() * s.getIncrement());
            reeval = true;
        }

        private void walkToBank(){
            while (calc.distanceTo(web.getNearestBank()) > 4)
                web.getWeb(web.getNearestBank()).step();
        }
        private void safeGuard(){
            if((lastLocation == getMyPlayer().getLocation()) && (lastMoveTime - (int)System.currentTimeMillis()) > 600000){
                log("Stuck for 10min! logging out...");
                sleep(random(4000, 5000));
                bank.close();
                game.logout(true);
                stopScript(true);
            }
            if(lastLocation != getMyPlayer().getLocation()){
                lastLocation = getMyPlayer().getLocation();
                lastMoveTime = (int)System.currentTimeMillis();
            }
        }
        private boolean areGlovesInBank(){
            if(bank.getCount(GLOVES_OF_SILENCE) > 0){
                glovesAreInBank = true;
                return true;
            }
            return false;
        }

        private boolean areSeedsInBank(){
            for(int k:KEEP_IDS){
                if(bank.getCount(k) > 0){
                    seedsAreInBank = true;
                    return true;
                }
            }
            return false;
        }

        private boolean withdrawFromBank(int item, int count, String name){
            if(bank.getCount(item) < count){
                log("Not enough " + name + " left");
                return false;
            }

            int i = 0;
            int tempCount = count;
            int start = inventory.getCount(item);
            Timer fiveCount = new Timer(5000);
            while(i < 5){
                bank.withdraw(item, tempCount);
                fiveCount.reset();
                while (inventory.getCount(item) == (start) && !inventory.isFull() && fiveCount.isRunning()){
                    sleep(100);
                }
                if(inventory.isFull())
                    return true;
                if(inventory.getCount(item) < count)
                    tempCount = count - inventory.getCount(item);
                else
                    return true;
                i++;
            }
            log("Could not get all of " + name + " after a 5 tries");
            return false;
        }

        private void outOfFood(){
            log("Could not find any food in your bank, check your tabs, or the food ID");
            log("Out of food! logging out...");
            sleep(random(4000, 5000));
            bank.close();
            game.logout(true);
            stopScript(true);
        }


        @SuppressWarnings("static-access")
        public synchronized int loop(){
            try {
                safeGuard();
                if(foodID != 0 && !foodStarted)
                    new FoodThread().start();
                if(priceDone && foodDone && !reeval)
                    reevaluateProfit();

                if(store.isOpen())
                    closeWindows();

                mouse.setSpeed(random(4 + mouseDelay, 7 + mouseDelay));

                antiBan(false);

                if(walking.getEnergy() < random(15 , 25))
                    walking.rest(random(85 , 100));

                if(!walking.isRunEnabled())
                    walking.setRun(true);

                if(isHealthLow() && useFood){
                    eatFood();
                    Timer t = new Timer(5000);
                    while(isHealthLow() && t.isRunning()){}
                }
                if(!game.inRandom() || !game.isLoginScreen() ||
                        !game.isWelcomeScreen() || interfaces.getComponent(906,231).isValid()){
                    env.disableRandoms();;
                }

                switch (getEnum()){
                    case RANDOM:
                    {
                        env.disableRandoms();
                        if((interfaces.getComponent(906,236).isValid() &&
                                interfaces.getComponent(906,236).containsText("Back") &&
                                interfaces.getComponent(906,231).isValid() &&
                                interfaces.getComponent(906,231).containsAction("Back"))){
                            interfaces.getComponent(906,231).doClick();
                            return 1000;
                        }
                        if((interfaces.getComponent(906,173).isValid() &&
                                interfaces.getComponent(906,173).containsText("Play") &&
                                interfaces.getComponent(906,160).isValid() &&
                                interfaces.getComponent(906,160).containsAction("Play"))){
                            interfaces.getComponent(906,160).doClick();
                            return 1000;
                        }
                        env.enableRandoms();
                        return 1000;
                    }
                    case WALK_TO_BANK:
                    {
                        closeWindows();
                        walkToBank();
                        return random(1000, 3000);
                    }
                    case DROP_SEEDS:
                    {
                        closeWindows();
                        dropJunkSeeds();
                        return random(1000 , 2000);
                    }
                    case EQUIP_GLOVES:
                    {
                        closeWindows();
                        try {
                            if(inventory.getItem(GLOVES_OF_SILENCE).doClick(true)){
                                handID = equipment.getItem(equipment.HANDS).getID();
                                glovesUsed++;
                            }
                        } catch (Exception e) {log("Could not equip gloves...");return random(1000, 2000);}
                        equipNewGloves = false;
                        return random(1000 , 3000);
                    }
                    case DEPOSIT:
                    {
                        if(dropSeeds){
                            closeWindows();
                            dropJunkSeeds();
                        }
                        if(!BANK_AREA.contains(getMyPlayer().getLocation())){
                            status = "Walking To Bank";
                            walkToBank();
                            return random(1000,3000);
                        }
                        while(!bank.isOpen())
                            openBank();
                        sleep(random(1000,2000));

                        if(!inventory.contains(foodID) &&
                                inventory.getCount() < 24 &&
                                adjustFood && withdrawFood < 25)
                            withdrawFood++;
                        if(inventory.getCount() > 0)
                            bank.depositAll();
                        highSeedsAreFull = false;
                        if(canWearGloves)
                            areGlovesInBank();
                        if(keepHighSeeds)
                            areSeedsInBank();
                        if(useFood){
                            if(carriedFood == null){
                                carriedFood = getFood();
                                foodID = carriedFood.getID();
                            }
                            if(bank.getCount(foodID) == 0){
                                outOfFood();
                            }
                        }
                        return random(1000, 2000);
                    }
                    case EAT_FOOD:
                    {
                        closeWindows();
                        eatFood();
                        while(isHealthLow()){}
                        return random(1000,1200);
                    }
                    case WITHDRAW_GLOVES:
                    {

                        if (handID != GLOVES_OF_SILENCE){
                            withdrawFromBank(GLOVES_OF_SILENCE, glovesToKeep + 1, "Gloves of silence");
                            return random(250, 1000);
                        }else
                            withdrawFromBank(GLOVES_OF_SILENCE, (glovesToKeep - inventory.getCount(GLOVES_OF_SILENCE)), "Gloves of silence");
                        return random(1000, 2000);
                    }
                    case WITHDRAW_SEEDS:
                    {
                        for(Seed s: highSeedArray){
                            if(inventory.getCount(s.getID()) == 0 && bank.getCount(s.getID()) != 0){
                                withdrawFromBank(s.getID(), 1, s.getName());
                                return random(250 , 750);
                            }
                        }
                        highSeedsAreFull = true;
                        return random(500,1000);
                    }
                    case WITHDRAW_FOOD:
                    {
                        if(keepHighSeeds){
                            if(!withdrawFromBank(foodID, 28, "food"))
                                outOfFood();
                        }else if (!withdrawFromBank(foodID, withdrawFood, "food"))
                            outOfFood();
                        while(bank.isOpen())
                            bank.close();

                        return random(1000, 2000);
                    }
                    case WALK_TO_MARKET:
                    {
                        while (!MARKET_AREA.contains(getMyPlayer().getLocation()))
                            web.getWeb(MARKET_AREA.getCentralTile()).step();
                    }
                    case FAILED_PICK:
                    {
                        closeWindows();
                        if(isHealthLow()){
                            eatFood();
                            antiBan(true);
                        }
                        dropJunkSeeds();
                        antiBan(true);
                        failedPick = false;
                        while(isHealthLow()){}
                        return random(200,500);
                    }
                    case STEAL:
                    {
                        closeWindows();
                        masterFarmer = npcs.getNearest(FARMER_ID);
                        if(masterFarmer.isOnScreen()){
                            status = "Pickpocketing!";
                            masterFarmer.interact("Pickpocket");
                            antiBan(false);
                            return random(200 + clickDelay , 400 + clickDelay);
                        }else{
                            status = "Walking to Farmer";
                            walking.walkTileOnScreen(masterFarmer.getLocation());
                            antiBan(false);
                            return random(200, 500);
                        }
                    }


                }
            } catch (Exception e) {}
            return random(1000, 2000);
        }

        private class CameraHandler extends Thread {
            @Override
            public void run() {
                try {
                    while (threadRun) {
                        int loop = cameraLoop();
                        if (loop < 0) {
                            break;
                        }
                        Thread.sleep(loop);
                    }
                } catch(Exception e) {
                    log("Camera handling thread died.");
                }
            }
        }
        int cameraLoop() {
            if (isPaused()) {
                return 1000;
            }
            if (bank.isOpen()) {
                return 100;
            }
            try {

                if(MARKET_AREA.contains(getMyPlayer().getLocation())){
                    int cameraAngle;
                    masterFarmer = npcs.getNearest(FARMER_ID);
                    if(camera.getAngle() - camera.getCharacterAngle(masterFarmer) < 0)
                        cameraAngle = camera.getCharacterAngle(masterFarmer) - camera.getAngle();
                    else
                        cameraAngle = camera.getAngle() - camera.getCharacterAngle(masterFarmer);
                    if (calc.distanceTo(masterFarmer.getLocation()) <= 8 && cameraAngle > 75){
                        camera.turnTo(masterFarmer, 50);
                        camera.setPitch(true);
                    }
                }else if(BANK_AREA.contains(getMyPlayer().getLocation())){
                    int cameraAngle;
                    RSObject bankBooth = objects.getNearest(Bank.BANK_BOOTHS);
                    if(camera.getAngle() - camera.getObjectAngle(bankBooth) < 0)
                        cameraAngle = camera.getObjectAngle(bankBooth) - camera.getAngle();
                    else
                        cameraAngle = camera.getAngle() - camera.getObjectAngle(bankBooth);
                    if (calc.distanceTo(bankBooth.getLocation()) <= 5 && cameraAngle > 75){
                        camera.turnTo(bankBooth, 50);
                        camera.setPitch(true);
                    }
                }
            } catch (Exception e) {return 10000;}
            return random(1000,6000);
        }

        private void antiBan(boolean overRide){
            int randomNo = random(1, 200);
            int r = random(1, 7);
            if (randomNo == 3 || overRide) {
                switch(r){
                    case 1:{
                        status = "AB Checking Our Hand Slot";
                        if(equipment.getItem(Equipment.HANDS).getID()  != GLOVES_OF_SILENCE)
                            equipNewGloves = true;
                    }break;
                    case 2:{
                        status = "AB Open Random Tab 1s - 2s";
                        game.getRandomTab();
                        sleep(random(1000 , 2000));
                    }break;
                    case 3: {
                        status = "AB Move Mouse Slightly .5s - 1s";
                        mouse.moveSlightly();
                        sleep(random(500 , 1000));
                    }break;
                    case 4: {
                        status = "AB Move Mouse .5s - 1s";
                        mouse.moveRandomly(70, 380);
                        sleep(random(500 , 1000));
                    }break;
                    case 5: {
                        status = "AB Move Off Screen 4s - 5s";
                        mouse.moveOffScreen();
                        sleep(4000 , 5000);
                    }break;
                    case 6: {
                        status = "AB Move Mouse Slightly .5s - 1s";
                        mouse.moveSlightly();
                        sleep(random(500 , 1000));
                    }break;
                    case 7: {
                        status = "AB Look AT Thieving Skill 4s - 5s";
                        if (game.getTab() != Tab.STATS) {
                            game.openTab(Tab.STATS);
                            sleep(random(500, 700));
                            mouse.move(random(620 , 665), random(295, 315));
                            sleep(random(3500, 5000));
                        }
                    }break;
                }
            }
            if(!useBanking)
                game.openTab(Tab.INVENTORY);
        }


        @Override
        public void messageReceived(MessageEvent e)
        {
            String svrmsg = e.getMessage();
            if (svrmsg.contains("stunned") && !lastMsg.contains("stunned")){
                failedPick = true;
                failPickpockets++;
            }

            if (svrmsg.contains("restores") && !lastMsg.contains("restores"))
                foodEaten++;

            if (svrmsg.toLowerCase().contains("strange")) {
                log("Strange Rock Detected");
                sleep(random(1500, 2500));
                if (interfaces.canContinue()) {
                    interfaces.clickContinue();
                }
            }

            if (svrmsg.contains("advanced")) {
                sleep(random(1500, 2500));
                if (interfaces.canContinue()) {
                    interfaces.clickContinue();
                }
                gainedLvls++;
                sleep(random(50,100));
            }

            if (svrmsg.contains("You pick"))
                succeedPickpockets++;

            if (svrmsg.toLowerCase().contains("gloves of silence have worn out")) {
                equipNewGloves = true;
            }

            if (svrmsg.contains("You steal") && svrmsg.contains("seed")){
                int seedCount = 1;
                String splitString[] = svrmsg.toLowerCase().split(" " , 10);
                String seedName = splitString[3];
                if (!splitString[2].contains("a") && !splitString[2].contains("an"))
                    seedCount = Integer.parseInt(splitString[2].trim());
                for(Seed s: seedArray){
                    if(s.getName().contains(seedName)){
                        s.setIncrement(seedCount);
                        if(!dropSeeds)
                            profit += (s.getPrice() * seedCount);
                        else if(s.getPrice() > minGoldToKeep)
                            profit += (s.getPrice() * seedCount);
                    }
                }
            }


            lastMsg = svrmsg;
        }



        @SuppressWarnings("serial")
        public class konzyMasterStealerGUI extends JFrame {

            private final File saveFile = new File(getCacheDirectory() +
                    System.getProperty("file.separator") +
                    String.valueOf(Math.abs(account.getName().hashCode())) + ".ini");


            private JCheckBox dropJunkSeedsCheckBox, skipBankingCheckBox, screenOnExitCheckBox, foodAdjustCheckBox, highSeedCheckBox;
            private JTextField foodOverrideTextBox, foodCountTextBox, bailHPTextBox, glovesCountTextBox;
            private JLabel dropSeedLabel, topLabel, skipBankingLabel, foodCountLabel, foodOverrideLabel,
                    bailHPLabel, screenOnExitLabel, stealSpeedLabel, mouseSpeedLabel, foodAdjustLabel,
                    highSeedLabel1, highSeedLabel2, highSeedLabel3, highSeedLabel4, glovesCountLabel;
            private JButton startButton;
            private JSlider stealSpeedSlider, mouseSpeedSlider;

            /** Creates new form NewJFrame */
            public konzyMasterStealerGUI() {
                this.setLocationRelativeTo(null);
                initComponents();
                pack();
                setVisible(true);
            }

            private void initComponents() {

                Properties props = loadProperties();

                topLabel = new javax.swing.JLabel();
                dropSeedLabel = new javax.swing.JLabel();
                skipBankingLabel = new javax.swing.JLabel();
                screenOnExitLabel = new javax.swing.JLabel();
                foodAdjustCheckBox = new javax.swing.JCheckBox();
                foodAdjustLabel = new javax.swing.JLabel();
                foodCountLabel = new javax.swing.JLabel();
                foodOverrideLabel = new javax.swing.JLabel();
                bailHPLabel = new javax.swing.JLabel();
                highSeedLabel1 = new javax.swing.JLabel();
                highSeedLabel2 = new javax.swing.JLabel();
                highSeedLabel3 = new javax.swing.JLabel();
                highSeedLabel4 = new javax.swing.JLabel();
                highSeedCheckBox = new javax.swing.JCheckBox();
                stealSpeedSlider = new javax.swing.JSlider();
                glovesCountLabel = new javax.swing.JLabel();
                glovesCountTextBox = new javax.swing.JTextField();
                foodCountTextBox = new javax.swing.JTextField();
                foodOverrideTextBox = new javax.swing.JTextField();
                bailHPTextBox = new javax.swing.JTextField();
                dropJunkSeedsCheckBox = new javax.swing.JCheckBox();
                skipBankingCheckBox = new javax.swing.JCheckBox();
                screenOnExitCheckBox = new javax.swing.JCheckBox();
                stealSpeedLabel = new javax.swing.JLabel();
                mouseSpeedLabel = new javax.swing.JLabel();
                mouseSpeedSlider = new javax.swing.JSlider();
                startButton = new javax.swing.JButton();

                setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
                topLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
                topLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                topLabel.setText("konzy's Master Farmer Stealer");

                dropSeedLabel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
                dropSeedLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
                dropSeedLabel.setText("Drop seeds worth less than 50G");

                skipBankingLabel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
                skipBankingLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
                skipBankingLabel.setText("Disable banking");

                screenOnExitLabel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
                screenOnExitLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
                screenOnExitLabel.setText("Screenshot on exit");

                foodAdjustLabel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
                foodAdjustLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
                foodAdjustLabel.setText("Automatic food adjustment");

                foodCountLabel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
                foodCountLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
                foodCountLabel.setText("How much food to withdraw 0 - 25");

                foodOverrideLabel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
                foodOverrideLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
                foodOverrideLabel.setText("Food ID or put food in inventory");

                bailHPLabel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
                bailHPLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
                bailHPLabel.setText("HP to bail at");

                highSeedLabel1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
                highSeedLabel1.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
                highSeedLabel1.setText("Keep high value seeds in inventory this includes");

                highSeedLabel2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
                highSeedLabel2.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
                highSeedLabel2.setText("Ranarr, Toadflax, Avantoe, Kwarm");

                highSeedLabel3.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
                highSeedLabel3.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
                highSeedLabel3.setText("Snapdragon, Cadantine, Lantadyme");

                highSeedLabel4.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
                highSeedLabel4.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
                highSeedLabel4.setText("Dwarf, Torstol and Watermelon");

                stealSpeedSlider.setMaximum(500);
                stealSpeedSlider.setMinorTickSpacing(50);
                stealSpeedSlider.setPaintTicks(true);
                stealSpeedSlider.setSnapToTicks(true);
                stealSpeedSlider.setValue(500);

                foodCountTextBox.setText("5");

                foodOverrideTextBox.setText(Integer.toString(foodID));

                bailHPTextBox.setText("40");

                stealSpeedLabel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
                stealSpeedLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
                stealSpeedLabel.setText("How fast to steal (slow to fast)");

                mouseSpeedLabel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
                mouseSpeedLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
                mouseSpeedLabel.setText("How fast to move mouse (slow to fast)");

                mouseSpeedSlider.setMaximum(10);
                mouseSpeedSlider.setMinimum(1);
                mouseSpeedSlider.setMinorTickSpacing(1);
                mouseSpeedSlider.setPaintTicks(true);
                mouseSpeedSlider.setSnapToTicks(true);

                glovesCountLabel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
                glovesCountLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
                glovesCountLabel.setText("Gloves to keep in inventory");

                glovesCountTextBox.setText("0");

                startButton.setText("Start");
                startButton.addActionListener(onStart);

                highSeedCheckBox.addActionListener(highSeedClicked);


                if(props.getProperty("screenOnExit") != null){
                    if(props.getProperty("screenOnExit").equals("true"))
                        screenOnExitCheckBox.setSelected(true);
                }
                if(props.getProperty("dropJunkSeeds") != null){
                    if(props.getProperty("dropJunkSeeds").equals("true"))
                        dropJunkSeedsCheckBox.setSelected(true);
                }
                if(props.getProperty("skipBanking") != null) {
                    if(props.getProperty("skipBanking").equals("true"))
                        skipBankingCheckBox.setSelected(true);
                }
                if(props.getProperty("foodAdjust") != null) {
                    if(props.getProperty("foodAdjust").equals("true"))
                        foodAdjustCheckBox.setSelected(true);
                }
                if(props.getProperty("highSeeds") != null) {
                    if(props.getProperty("highSeeds").equals("true"))
                        highSeedCheckBox.setSelected(true);
                }
                if(props.getProperty("glovesCount") != null) {
                    glovesCountTextBox.setText(props.getProperty("glovesCount"));
                }
                if(props.getProperty("foodOverride") != null) {
                    foodOverrideTextBox.setText(props.getProperty("foodOverride"));
                }
                if(props.getProperty("foodCount") != null) {
                    foodCountTextBox.setText(props.getProperty("foodCount"));
                }
                if(props.getProperty("bailHP") != null) {
                    bailHPTextBox.setText(props.getProperty("bailHP"));
                }
                if(props.getProperty("mouseSpeed") != null) {
                    mouseSpeedSlider.setValue(Integer.parseInt(props.getProperty("mouseSpeed")));
                }
                if(props.getProperty("stealSpeed") != null) {
                    stealSpeedSlider.setValue(Integer.parseInt(props.getProperty("stealSpeed")));
                }

                if(highSeedCheckBox.isSelected()){
                    dropJunkSeedsCheckBox.setEnabled(false);
                    dropJunkSeedsCheckBox.setSelected(false);
                    foodAdjustCheckBox.setEnabled(false);
                    foodAdjustCheckBox.setSelected(false);
                    foodCountTextBox.setEnabled(false);
                    glovesCountTextBox.setEnabled(true);
                }else{
                    dropJunkSeedsCheckBox.setEnabled(true);
                    foodAdjustCheckBox.setEnabled(true);
                    foodCountTextBox.setEnabled(true);
                    glovesCountTextBox.setEnabled(false);
                }


                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(topLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE)
                                .addGroup(layout.createSequentialGroup()
                                        .addGap(10, 10, 10)
                                        .addComponent(dropSeedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(dropJunkSeedsCheckBox)
                                        .addGap(29, 29, 29))
                                .addGroup(layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(skipBankingLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(skipBankingCheckBox)
                                        .addContainerGap(146, Short.MAX_VALUE))
                                .addGroup(layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                        .addComponent(stealSpeedLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(stealSpeedSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                        .addComponent(mouseSpeedLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(mouseSpeedSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                                        .addComponent(foodCountLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addGap(18, 18, 18)
                                                                        .addComponent(foodCountTextBox, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE))
                                                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                                        .addComponent(foodOverrideLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addGap(18, 18, 18)
                                                                        .addComponent(foodOverrideTextBox, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE))
                                                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                                        .addComponent(bailHPLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addGap(18, 18, 18)
                                                                        .addComponent(bailHPTextBox, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)))
                                                        .addGap(98, 98, 98)))
                                        .addContainerGap())
                                .addGroup(layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(foodAdjustLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(foodAdjustCheckBox))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(screenOnExitLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(screenOnExitCheckBox)))
                                        .addContainerGap(146, Short.MAX_VALUE))
                                .addGroup(layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(highSeedLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap(185, Short.MAX_VALUE))
                                .addGroup(layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(highSeedLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(highSeedLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(highSeedLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addComponent(highSeedCheckBox)
                                        .addContainerGap(146, Short.MAX_VALUE))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addContainerGap(209, Short.MAX_VALUE)
                                        .addComponent(startButton)
                                        .addGap(191, 191, 191))
                                .addGroup(layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(glovesCountLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(glovesCountTextBox, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
                                        .addGap(108, 108, 108))
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(topLabel)
                                        .addGap(18, 18, 18)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(dropSeedLabel)
                                                .addComponent(dropJunkSeedsCheckBox))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(skipBankingLabel)
                                                .addComponent(skipBankingCheckBox))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(screenOnExitLabel)
                                                .addComponent(screenOnExitCheckBox))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(foodAdjustLabel)
                                                .addComponent(foodAdjustCheckBox))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(highSeedLabel1)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(layout.createSequentialGroup()
                                                        .addGap(18, 18, 18)
                                                        .addComponent(highSeedCheckBox))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(highSeedLabel2)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(highSeedLabel3)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(highSeedLabel4)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(glovesCountLabel)
                                                .addComponent(glovesCountTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(foodCountLabel)
                                                .addComponent(foodCountTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(foodOverrideLabel)
                                                .addComponent(foodOverrideTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(bailHPLabel)
                                                .addComponent(bailHPTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(stealSpeedSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(stealSpeedLabel))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(mouseSpeedSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(mouseSpeedLabel))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(startButton)
                                        .addGap(7, 7, 7))
                );
            }


            private Properties loadProperties() {
                try {
                    if(!saveFile.exists())
                        saveFile.createNewFile();
                    Properties p = new Properties();
                    p.load(new FileInputStream(saveFile));
                    return p;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            private void saveProperties() {
                Properties p = new Properties();
                p.put("screenOnExit", Boolean.toString(screenOnExitCheckBox.isSelected()));
                p.put("dropJunkSeeds", Boolean.toString(dropJunkSeedsCheckBox.isSelected()));
                p.put("skipBanking", Boolean.toString(skipBankingCheckBox.isSelected()));
                p.put("foodAdjust", Boolean.toString(foodAdjustCheckBox.isSelected()));
                p.put("highSeeds", Boolean.toString(highSeedCheckBox.isSelected()));
                p.put("glovesCount", glovesCountTextBox.getText());
                p.put("foodCount", foodCountTextBox.getText());
                p.put("bailHP", bailHPTextBox.getText());
                p.put("mouseSpeed", Integer.toString(mouseSpeedSlider.getValue()));
                p.put("stealSpeed", Integer.toString(stealSpeedSlider.getValue()));

                carriedFood = getFood();
                if (carriedFood != null){
                    try {
                        foodID = carriedFood.getID();
                    } catch (Exception e) {}
                    p.put("foodOverride", "" + foodID);
                }else
                    p.put("foodOverride", foodOverrideTextBox.getText());

                try {
                    p.store(new FileOutputStream(saveFile), "");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            private ActionListener highSeedClicked = new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(highSeedCheckBox.isSelected()){
                        dropJunkSeedsCheckBox.setEnabled(false);
                        dropJunkSeedsCheckBox.setSelected(false);
                        foodAdjustCheckBox.setEnabled(false);
                        foodAdjustCheckBox.setSelected(false);
                        foodCountTextBox.setEnabled(false);
                        glovesCountTextBox.setEnabled(true);
                    }else{
                        dropJunkSeedsCheckBox.setEnabled(true);
                        foodAdjustCheckBox.setEnabled(true);
                        foodCountTextBox.setEnabled(true);
                        glovesCountTextBox.setEnabled(false);
                    }
                }
            };



            private ActionListener onStart = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    saveProperties();
                    dropSeeds = dropJunkSeedsCheckBox.isSelected();
                    useBanking = !skipBankingCheckBox.isSelected();
                    screenshotOnExit = screenOnExitCheckBox.isSelected();
                    adjustFood = foodAdjustCheckBox.isSelected();
                    keepHighSeeds = highSeedCheckBox.isSelected();
                    clickDelay = stealSpeedSlider.getMaximum() - stealSpeedSlider.getValue();
                    mouseDelay = mouseSpeedSlider.getMaximum() - mouseSpeedSlider.getValue();

                    try {
                        foodID = Integer.parseInt(foodOverrideTextBox.getText());
                        withdrawFood = Integer.parseInt(foodCountTextBox.getText());
                        if(withdrawFood > 25)
                            withdrawFood = 25;
                        if(withdrawFood == 0)
                            useFood = false;
                        bailHP = Integer.parseInt(bailHPTextBox.getText());
                        glovesToKeep = Integer.parseInt(glovesCountTextBox.getText());
                    } catch (NumberFormatException e1) {}
                    guiWait = false;
                    dispose();
                }
            };
        }

        private String convertTime(int time){
            int hours = (int) time / (60 * 1000 * 60);
            int minutes = (int) (time - (hours * 60 * 1000 * 60)) / (60 * 1000);
            int seconds = (int) (time - (hours * 60 * 1000 * 60) - (minutes * 60 * 1000)) / 1000;


            String normMinutes;
            String normSeconds;

            if (minutes < 10)
                normMinutes = "0" + minutes;
            else normMinutes = "" + minutes;
            if (seconds < 10)
                normSeconds = "0" + seconds;
            else normSeconds = "" + seconds;

            return hours + ":" + normMinutes + ":" + normSeconds;
        }

        //START: Paint generated using Enfilade's Easel
        private final Color BLACK = new Color(0, 0, 0);
        private final Color GREEN = new Color(0, 208, 0);
        private final Color WHITE = new Color(255, 255, 255);
        private final Color RED = new Color(255, 0, 0);

        private final Font ARIAL = new Font("Arial", 0, 13);

        public void onRepaint(Graphics g1) {

            if(!showPaint){
                Graphics2D g = (Graphics2D)g1;
                g.setFont(ARIAL);
                g.setColor(WHITE);
                g.drawString("Click Chat To See Paint", 160, 335);
                return;
            }

            int runTime = (int) (System.currentTimeMillis() - startTime);
            String convertedRunTime = convertTime(runTime);


            int xpToNextLVL = skills.getExpToNextLevel(Skills.THIEVING);
            long currentXP = skills.getCurrentExp(Skills.THIEVING);
            int currentLVL = skills.getCurrentLevel(Skills.THIEVING);
            int gainedXP = (int)(currentXP - startXP);
            int xpPerHour = (int) ((3600000.0 / runTime) * gainedXP);


            int timeToLevel = (int) (skills.getExpToNextLevel(Skills.THIEVING) / (double) ((3600000.0 / runTime) * gainedXP) * 3600000);
            String convertedTimeToLevel = "Infinity!";
            if(timeToLevel < (3600000 * 48)){//<48hrs
                convertedTimeToLevel = convertTime(timeToLevel);
            }
            int succeedPickpocketsHour = (int) ((3600000.0 / runTime) * succeedPickpockets);
            int profitPerHour = (int) ((3600000.0 / runTime) * profit);

            String dispProfit = "" + profit;
            String dispProfitPerHour = "" + profitPerHour;
            if(profit < 0){
                dispProfit = "(" + (-profit) + ")";
                dispProfitPerHour = "(" + (-profitPerHour) + ")";
            }
            if(!reeval){
                dispProfit += " Processing...";
                dispProfitPerHour += " Processing...";
            }



            Graphics2D g = (Graphics2D)g1;
            g.setColor(BLACK);
            g.fillRect(8, 344, 505, 128);
            g.setColor(RED);
            g.fillRoundRect(13, 453, 499, 17, 16, 16);
            g.setFont(ARIAL);
            g.setColor(GREEN);
            g.drawString("konzy's Master Stealer V " + properties.version(), 10, 355);
            g.drawString("XP Gained: " + gainedXP, 10, 370);
            g.drawString("XP/hr: " + xpPerHour, 10, 385);
            g.drawString("Current Level: " + currentLVL, 10, 400);
            g.drawString("XP needed to Level up: " + xpToNextLVL, 10, 415);
            g.drawString("Time to Next Level " + convertedTimeToLevel, 10, 430);
            g.drawString("Run Time: " + convertedRunTime, 10, 445);
            g.drawString("Picks per Hour: " + succeedPickpocketsHour, 250, 370);
            g.drawString("Picks Failed: " + failPickpockets, 250, 385);
            g.drawString("Profit: " + dispProfit, 250, 400);
            g.drawString("Profit Per Hour: " + dispProfitPerHour, 250, 415);
            g.drawString("Status: " + status, 250, 430);
            g.drawString("By konzy", 250, 445);
            if(outOfDate){
                g.setColor(RED);
                g.drawString("Please Update to " + currentVersion, 250, 355);
            }else{
                g.setColor(GREEN);
                g.drawString("Script is Current", 250, 355);
            }


            g.fillRoundRect(13, 453, (495 * skills.getPercentToNextLevel(Skills.THIEVING)) / 100, 17, 16, 16);
            g.setColor(WHITE);
            g.drawString("Click Paint To See Chat", 160, 335);
            g.drawString((skills.getPercentToNextLevel(Skills.THIEVING) + " %"), ((495 * skills.getPercentToNextLevel(Skills.THIEVING)) / 200), 467);


            Point x = mouse.getLocation();
            Point y = mouse.getPressLocation();

            g.setColor(Color.green);

            g.drawOval(x.x - 2, x.y - 2, 5, 5);
            g.drawOval(x.x - 5, x.y - 5, 11, 11);

            if ((System.currentTimeMillis() - mouse.getPressTime()) < 3000) {
                g.setColor(Color.red);
                g.drawOval(y.x - 2, y.y - 2, 4, 4);
                g.drawOval(y.x - 5, y.y - 5, 10, 10);
            }



        }
        //END: Paint generated using Enfilade's Easel

        @Override
        public void mouseClicked(MouseEvent e) {
            RSComponent inter = interfaces.get(137).getComponent(0);
            if(inter.getArea().contains(e.getPoint())) {
                showPaint = !showPaint;
            }
        }

        @Override
        public void mousePressed(MouseEvent e) { }

        @Override
        public void mouseReleased(MouseEvent e) { }

        @Override
        public void mouseEntered(MouseEvent e) { }

        @Override
        public void mouseExited(MouseEvent e) { }
    }
