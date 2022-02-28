import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


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
    static GeneradorXML generadorXML;

    static final String xmlDestino = "gp.xml";

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

        generadorXML = new GeneradorXML(xmlDestino, docGP);

        long start = System.currentTimeMillis();

        //region Obtención de los datos
        circuitsHashMap = obtenerCircuits();
        lapTimesHashMap = obtenerLapTimes();
        bestLapTimesHashMap = obtenerVueltaMasRapidaDeCadaCarrera(lapTimesHashMap);

        racesHasMap = obtenerRaces(circuitsHashMap, bestLapTimesHashMap);
        racesPorSeasons = obtenerRacesPorSeasons(racesHasMap);
        //endregion

        escribirXML(docGP);
        generadorXML.guardarObjetoXMLEnFichero();

        long finish = System.currentTimeMillis();

        System.out.println("Tiempo: " + (float) ( finish - start ) / 1000.0 + "s");
    }

    //region Métodos
    /**
     * Devuelve un HasMap con todos los circuitos agrupados por su id.
     */
    static HashMap<Integer, Circuit> obtenerCircuits() throws IOException, SAXException
    {
        HashMap<Integer, Circuit> hashMap = new HashMap<>();

        final String xml = "circuits.xml";
        final Document doc = docBuilder.parse(xml);

        NodeList nodeList = doc.getElementsByTagName("Circuit");

        for (int i = 0; i < nodeList.getLength(); i++)
        {
            // Hacerlo de tipo Element en lugar de Node nos da la ventaja de poder obtener los nodos hijo
            // a partir del nombre de su etiqueta
            Element nodoCircuit = (Element) nodeList.item(i);

            //region Se obtienen los valores del xml
            int id = Integer.parseInt(getNodoTexto(nodoCircuit, "circuitId"));
            String name = getNodoTexto(nodoCircuit, "name");
            String location = getNodoTexto(nodoCircuit, "location");
            String country = getNodoTexto(nodoCircuit, "country");
            //endregion

            hashMap.put(
                    id,
                    new Circuit(id, name, location, country)
            );
        }
        return hashMap;
    }
    /**
     * Devuelve un HasMap con todos las carreras agrupadas por su id.
     */
    static HashMap<Integer, Race> obtenerRaces(HashMap<Integer, Circuit> circuitsHashMap, HashMap<Integer, LapTime> bestLapTimesHasMap) throws IOException, SAXException
    {
        HashMap<Integer, Race> hashMap = new HashMap<>();

        final String xml = "races.xml";
        final Document doc = docBuilder.parse(xml);

        NodeList nodeList = doc.getElementsByTagName("Race");

        for (int i = 0; i < nodeList.getLength(); i++)
        {
            Element nodo = (Element) nodeList.item(i);

            //region Se obtienen los valores del xml
            int id = Integer.parseInt(getNodoTexto(nodo, "raceId"));
            int circuitId = Integer.parseInt(getNodoTexto(nodo, "circuitId"));
            int year = Integer.parseInt(getNodoTexto(nodo, "year"));
            int round = Integer.parseInt(getNodoTexto(nodo, "round"));
            String date = getNodoTexto(nodo, "date");
            String time = getNodoTexto(nodo, "time");
            String url = getNodoTexto(nodo, "url");
            //endregion

            var race = new Race(id, year, round, date, time, url);
            race.circuit = circuitsHashMap.get(circuitId);
            race.bestLapTime = bestLapTimesHasMap.get(id);

            hashMap.put(id, race);
        }
        return hashMap;
    }
    /**
     * Devuelve un HasMap de todas las vueltas de cada carrera.
     */
    static HashMap<Integer, List<LapTime>> obtenerLapTimes() throws IOException, SAXException
    {
        HashMap<Integer, List<LapTime>> hashMap = new HashMap<>();

        final String xml = "lapTimes.xml";
        final Document doc = docBuilder.parse(xml);

        NodeList nodeList = doc.getElementsByTagName("lapTime");

        for (int i = 0; i < nodeList.getLength(); i++)
        {
            Element nodo = (Element) nodeList.item(i);

            //region Se obtienen los valores del xml
            int raceId = Integer.parseInt(getNodoTexto(nodo, "raceId"));
            int driverId = Integer.parseInt(getNodoTexto(nodo, "driverId"));
            int lap = Integer.parseInt(getNodoTexto(nodo, "lap"));
            String time = getNodoTexto(nodo, "time");
            //endregion

            var lapTime = new LapTime(raceId, driverId, lap, time);

            // Si no hay un ArrayList se añade
            hashMap.computeIfAbsent(raceId, v -> new ArrayList<>());

            hashMap.get(raceId).add(lapTime);
        }
        return hashMap;
    }
    /**
     * Devuelve un HasMap con la vuelta más rápida de cada carrera.
     */
    static HashMap<Integer, LapTime> obtenerVueltaMasRapidaDeCadaCarrera(HashMap<Integer, List<LapTime>> lapTimeHashMap)
    {
        HashMap<Integer, LapTime> hashMap = new HashMap<>();

        lapTimeHashMap.forEach((raceId, lapTimeList) ->
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
            hashMap.put(raceId, bestLapTime);
        });
        return hashMap;
    }
    /**
     * Devuelve un HashMap de listas de carreras agrupadas por temporada
     */
    static HashMap<Integer, List<Race>> obtenerRacesPorSeasons(HashMap<Integer, Race> racesHasMap)
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
    static void escribirXML(Document doc)
    {
        Element rootElement = doc.createElement("Temporadas");
        doc.appendChild(rootElement);

        racesPorSeasons.forEach((year, racesList) ->
        {
            Element temporada = doc.createElement("Temporada");

            racesList.forEach(race ->
            {
                Element nodoRace = generadorXML.objetoANodo(
                        "Race",
                        race,
                        new ArrayList<>(List.of("date", "bestLapTime")) // Atributos a ignorar
                );
                // Se añade date por separado para ponerlo con el formato de interés (dd/MM/yyy)
                Element nodoDate = doc.createElement("date");
                nodoDate.setTextContent(race.getDate());
                nodoRace.appendChild(nodoDate);

                // Se añade por separado, ya que no interesa añadir raceId (es redundante)
                if (race.bestLapTime != null)
                {
                    // Se añade por separado para que tenga el formato de interés (m:ss.SSS)
                    Element nodoTime = doc.createElement("time");
                    nodoTime.setTextContent(race.bestLapTime.getTime());

                    Element nodoBestLapTime = generadorXML.objetoANodo(
                            "bestLapTime",
                            race.bestLapTime,
                            new ArrayList<>(List.of("raceId", "time"))
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

    /**
     * Facilita el trabajo de obtener el texto de un determinado nodo.
     */
    static String getNodoTexto(Element nodo, String tag)
    {
        return nodo.getElementsByTagName(tag).item(0).getTextContent();
    }
    //endregion
}