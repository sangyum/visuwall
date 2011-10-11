package jersey_osgi.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Number {
    enum Location {LESS, FOUND, MORE};
    private static final int MAX_NUM = 10;
    
    static List<Number> NUMBERS;
    static {
        NUMBERS = new ArrayList<Number>();
        
        Random random = new Random(System.currentTimeMillis());
        int theOne = random.nextInt(MAX_NUM) + 1;
        
        for (int i=1; i <= MAX_NUM; i++) {
            Location location;
            if (i == theOne) {
                location = Location.FOUND;
            } else if (i < theOne) {
                location = Location.LESS;
            } else {
                location = Location.MORE;
            }
            NUMBERS.add(new Number(i, location));
        }
    };
    
    private final int number;
    private final Location location;
    public Number(int num, Location l) {
        number = num;
        location = l;
    }

    public int  getNumber() {
        return number;
    }    
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("I'm number ");
        sb.append(number);
        sb.append(". ");
        switch (location) {
        case FOUND:
            sb.append("You guessed right!");
            break;
        case LESS:
            sb.append("You need to guess higher.");
            break;
        case MORE:
            sb.append("You need to guess lower.");
            break;
        }
        return sb.toString();
    }
}
