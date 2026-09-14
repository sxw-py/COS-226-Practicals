import java.util.Random;

public class TaskGenerator 
{

    private static final String[] TASKS = 
    {
        "buttered the bread",
        "sheared the sheep",
        "polished the cat",
        "taught a pigeon to whistle",
        "counted the potatoes",
        "interviewed a suspicious-looking duck",
        "organised the socks by emotional maturity",
        "argued with the toaster",
        "gave the plant a performance review",
        "wrestled a bear... AND WON!",
        "ran for president",
        "learnt how to drive stick shift",
        "argued with the toaster",
        "trained for the Olympics",
        "challenged a squirrel to a duel",
        "built a treehouse",
        "opened a bakery",
        "became a professional fisherman",
        "taught the dog concurrency",
        "invented a new language",
        "won a staring contest",
        "attended a professional game of paddle",
        "learned to juggle",
        "became a chess grandmaster",
        "wrote a bestselling novel",
        "acheived world peace",
        "opened a detective agency",
        "entered a eating competition",
        "became a motivational speaker",
        "discovered a new species",
        "accidentally became mayor",
        "is finally awake, it walked right into that Imperial ambush"
    };

    private static final Random random = new Random();

    public static String getRandomTask() 
    {
        return TASKS[random.nextInt(TASKS.length)];
    }
}