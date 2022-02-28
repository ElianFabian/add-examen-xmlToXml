import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Race
{
    //region Atributos
    int id;
    int year;
    int round;
    Circuit circuit;
    Date date;
    String time;
    String url;
    LapTime bestLapTime;

    private static final SimpleDateFormat formatoDateEntrada = new SimpleDateFormat("d/M/yyyy");
    private static final SimpleDateFormat formatoDateSalida = new SimpleDateFormat("dd/MM/yyyy");
    //endregion

    //region Constructores
    public Race(int id, int year, int round, String date, String time, String url)
    {
        this.id = id;
        this.year = year;
        this.round = round;
        setDate(date);
        this.time = time;
        this.url = url;
    }
    public Race(){
        
    }
    //endregion
    
    //region Métodos
    void setDate(String date)
    {
        try
        {
            this.date = formatoDateEntrada.parse(date);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
    }
    String getDate() {
        return formatoDateSalida.format(date);
    }
    @Override public String toString()
    {
        return "Race{" +
                "id=" + id +
                ", year=" + year +
                ", round=" + round +
                ", circuit=" + circuit +
                ", date=" + getDate() +
                ", time='" + time + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
    //endregion
}
