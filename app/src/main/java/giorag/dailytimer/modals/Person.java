package giorag.dailytimer.modals;

import java.io.Serializable;

/**
 * Created by GioraPC on 03/03/2016.
 */
public class Person extends Object implements Serializable{
    public String name;
    public boolean available;

    public Person(String name, boolean available) {
        this.name = name;
        this.available = available;
    }
}
