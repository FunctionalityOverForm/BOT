package LOOT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

public class ItemLookup {

    private static String json;

    private ItemLookup() {
    }

    private static void initData() {
        try {
            URL url = new URL("https://rsbuddy.com/exchange/summary.json");
            URLConnection connect = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            json = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Optional<String> get(String itemName, LOOT.Property Property) {

        if (json == null) {
            initData();
        }

        Optional<String> itemBlock = Arrays
                .stream(json.split("\\{"))
                .filter(phrase -> findString(itemName, phrase))
                .findFirst();

        return itemBlock.flatMap(s -> Arrays
                .stream(s.split(","))
                .filter(phrase -> findString(Property.getProperty(), phrase))
                .map(ItemLookup::clean)
                .findFirst());
    }

    private static boolean findString(String target, String phrase) {
        return phrase.matches(".*\"" + Pattern.quote(target) + "\".*");
    }

    private static String clean(String value) {
        value = value.split(":")[1];
        return value.replaceAll("}", "").replaceAll("\"", "");
    }
}
