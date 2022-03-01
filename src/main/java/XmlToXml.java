import archivos.EscritorXML;
import archivos.LectorXML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


//Toma todas las temporadas (year) distintas de las que se dispone en races.xml.
//        De estos nodos deben colgar los datos de las n carreras de la temporada.
//        Ten en cuenta que con el circuitId tienes que “traer” algunos de los datos
//        del circuito y que está en circuits.xml, al menos el nombre, la localización y el país. (3 puntos).
//
//        Además, para cada carrera tiene que verse la vuelta más rápida, el récord de este gran premio.
//        Esto se obtiene desde lapTimes. (2 puntos).
//
//        Tienes que sacar con el id de la carrera, de todas las vueltas que haya, la que se ha dado
//        en un tiempo menor, dando el id del piloto, la vuelta y el tiempo. (2 puntos).
//
//        Con lo que tengas crea y guarda en disco gp.xml.


public class XmlToXml
{
    //region Atributos
    static DocumentBuilder docBuilder = null;
    static Document docGP;
    static EscritorXML escritorXML;

    static final String XML_DESTINO = "gp.xml";

    static HashMap<Integer, Circuit> circuitsHashMap;
    static HashMap<Integer, Race> racesHasMap;
    static HashMap<Integer, List<LapTime>> lapTimesHashMap;
    static HashMap<Integer, LapTime> bestLapTimesHashMap;
    static HashMap<Integer, List<Race>> racesPorSeasons;
    //endregion

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, TransformerException
    {
        docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        docGP = docBuilder.newDocument();

        escritorXML = new EscritorXML(XML_DESTINO, docGP);

        long start = System.currentTimeMillis();

        //region Obtención de los datos
        circuitsHashMap = obtenerCircuits();
        lapTimesHashMap = obtenerLapTimes();
        bestLapTimesHashMap = obtenerVueltaMasRapidaDeCadaCarrera(lapTimesHashMap);

        racesHasMap = obtenerRaces(circuitsHashMap, bestLapTimesHashMap);
        racesPorSeasons = obtenerRacesPorSeasons(racesHasMap);
        //endregion

        escribirXML(docGP);
        escritorXML.guardarObjetoXMLEnFichero();

        long finish = System.currentTimeMillis();

        System.out.println("Tiempo: " + (float) ( finish - start ) / 1000.0 + "s");
    }

    //region Métodos
    
    /**
     * Devuelve un HasMap con todos los circuitos agrupados por su id.
     */
    private static HashMap<Integer, Circuit> obtenerCircuits() throws IOException, SAXException
    {
        final String xml = "circuits.xml";

        final Document doc = docBuilder.parse(xml);

        var lectorXML = new LectorXML(xml, doc);
        HashMap<Integer, Circuit> hashMap = new HashMap<>();
        
        lectorXML.leerNodosPorNombre("Circuit", nodo ->
        {
            int id = nodo.getTextoInt("circuitId");
            String name = nodo.getTexto("name");
            String location = nodo.getTexto("location");
            String country = nodo.getTexto("country");

            hashMap.put(id, new Circuit(id, name, location, country));
        });

        return hashMap;
    }
    
    /**
     * Devuelve un HasMap con todos las carreras agrupadas por su id.
     */
    private static HashMap<Integer, Race> obtenerRaces(HashMap<Integer, Circuit> circuitsHashMap, HashMap<Integer, LapTime> bestLapTimesHasMap) throws IOException, SAXException
    {
        HashMap<Integer, Race> hashMap = new HashMap<>();

        final String xml = "races.xml";

        final Document doc = docBuilder.parse(xml);
        var lectorXML = new LectorXML(xml, doc);
        
        lectorXML.leerNodosPorNombre("Race", nodo ->
        {
            int id = nodo.getTextoInt("raceId");
            int circuitId = nodo.getTextoInt("circuitId");
            int year = nodo.getTextoInt("year");
            int round = nodo.getTextoInt("round");
            String date = nodo.getTexto("date");
            String time = nodo.getTexto("time");
            String url = nodo.getTexto("url");

            var race = new Race(id, year, round, date, time, url);
            race.circuit = circuitsHashMap.get(circuitId);
            race.bestLapTime = bestLapTimesHasMap.get(id);

            hashMap.put(id, race);
        });

        return hashMap;
    }
    
    /**
     * Devuelve un HasMap de todas las vueltas de cada carrera.
     */
    private static HashMap<Integer, List<LapTime>> obtenerLapTimes() throws IOException, SAXException
    {
        HashMap<Integer, List<LapTime>> hashMap = new HashMap<>();

        final String xml = "lapTimes.xml";

        final Document doc = docBuilder.parse(xml);
        var lectorXML = new LectorXML(xml, doc);
        
        lectorXML.leerNodosPorNombre("lapTime", nodo ->
        {
            int raceId = nodo.getTextoInt("raceId");
            int driverId = nodo.getTextoInt("driverId");
            int lap = nodo.getTextoInt("lap");
            String time = nodo.getTexto("time");

            var lapTime = new LapTime(raceId, driverId, lap, time);

            // Si no hay un ArrayList se añade
            hashMap.computeIfAbsent(raceId, v -> new ArrayList<>());

            hashMap.get(raceId).add(lapTime);
        });

        return hashMap;
    }

    /**
     * Devuelve un HasMap con la vuelta más rápida de cada carrera.
     */
    private static HashMap<Integer, LapTime> obtenerVueltaMasRapidaDeCadaCarrera(HashMap<Integer, List<LapTime>> lapTimeHashMap)
    {
        HashMap<Integer, LapTime> hashMap = new HashMap<>();

        lapTimeHashMap.forEach((raceId, lapTimeList) ->
        {
            var bestLapTime = obtenerElMejorLapTime(lapTimeList);
            
            hashMap.put(raceId, bestLapTime);
        });
        return hashMap;
    }

    /**
     * Dada una lista de lapTime devuelve el lapTime con menor tiempo.
     */
    private static LapTime obtenerElMejorLapTime(List<LapTime> lapTimeList)
    {
        LapTime bestLapTime = null;
        long bestTime = Long.MAX_VALUE;

        for (var lapTime : lapTimeList)
        {
            if (bestTime > lapTime.time.getTime())
            {
                bestTime = lapTime.time.getTime();
                bestLapTime = lapTime;
            }
        }
        return bestLapTime;
    }
    
    /**
     * Devuelve un HashMap de listas de carreras agrupadas por temporada
     */
    private static HashMap<Integer, List<Race>> obtenerRacesPorSeasons(HashMap<Integer, Race> racesHasMap)
    {
        HashMap<Integer, List<Race>> hashMap = new HashMap<>();

        for (var r : racesHasMap.entrySet())
        {
            // Si no hay un ArrayList se añade
            hashMap.computeIfAbsent(r.getValue().year, v -> new ArrayList<>());

            hashMap.get(r.getValue().year).add(r.getValue());
        }
        return hashMap;
    }
    /**
     * Pasa los objetos a XML.
     */
    private static void escribirXML(Document doc)
    {
        var rootElement = doc.createElement("Temporadas");
        doc.appendChild(rootElement);

        racesPorSeasons.forEach((year, racesList) ->
        {
            var temporada = doc.createElement("Temporada");

            racesList.forEach(race ->
            {
                Element nodoRace = escritorXML.objetoANodo(
                        "Race",
                        race,
                        Set.of("date", "bestLapTime") // Atributos a ignorar
                );
                
                // Se añade date por separado para ponerlo con el formato de interés (dd/MM/yyy)
                var nodoDate = doc.createElement("date");
                nodoDate.setTextContent(race.getDate());
                nodoRace.appendChild(nodoDate);

                // Se añade por separado, ya que no interesa añadir raceId (es redundante)
                if (race.bestLapTime != null)
                {
                    // Se añade por separado para que tenga el formato de interés (m:ss.SSS)
                    var nodoTime = doc.createElement("time");
                    nodoTime.setTextContent(race.bestLapTime.getTime());

                    var nodoBestLapTime = escritorXML.objetoANodo(
                            "bestLapTime",
                            race.bestLapTime,
                            Set.of("raceId", "time")
                    );
                    
                    nodoBestLapTime.appendChild(nodoTime);
                    nodoRace.appendChild(nodoBestLapTime);
                }
                temporada.appendChild(nodoRace);
            });
            
            temporada.setAttribute("year", year.toString());
            rootElement.appendChild(temporada);
        });
    }
    
    //endregion
}