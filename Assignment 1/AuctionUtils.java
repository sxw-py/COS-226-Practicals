import java.util.Random;

public final class AuctionUtils 
{

    private static final String[] ITEM_NAMES = 
    {
        "Mclaren F1",
        "Jaguar E Type",
        "Ferrari 250 GTO",
        "Fiat 500",
        "Lancia Stratos",
        "Lancia Delta HF Integrale",
        "Ferrari 250 California",
        "Mercedes-Benz 300 SL",
        "Ford Mustang",
        "Shelby Cobra"
    };

    private AuctionUtils() 
    {
    
    }

    public static String generateItemName() 
    {

        Random random = new Random();
        return ITEM_NAMES[random.nextInt(ITEM_NAMES.length)];
    }
}