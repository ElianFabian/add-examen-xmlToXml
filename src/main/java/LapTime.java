import archivos.EscritorXML;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LapTime implements EscritorXML.ObjetoXML
{
    //region Atributos
    int raceId;
    int driverId;
    int lap;
    Date time;

    private static final SimpleDateFormat formatoTime = new SimpleDateFormat("m:ss.SSS");
    //endregion

    //region Construtores
    public LapTime(int raceId, int driverId, int lap, String time)
    {
        this.raceId = raceId;
        this.driverId = driverId;
        this.lap = lap;
        setTime(time);
    }
    //endregion

    //region Métodos
    String getTime()
    {
        return formatoTime.format(time);
    }
    void setTime(String time)
    {
        try
        {
            this.time = formatoTime.parse(time);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
    }
    
    @Override public String toString()
    {
        return "LapTime{" +
                "raceId=" + raceId +
                ", driverId=" + driverId +
                ", lap=" + lap +
                ", time=" + getTime() +
                '}';
    }
    //endregion
}
