package archivos;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.function.Consumer;

public class LectorXML
{
    String ficheroXML;
    Document doc;

    public LectorXML(String ficheroXML, Document doc)
    {
        this.ficheroXML = ficheroXML;
        this.doc = doc;
    }

    /**
     * Lee todos los nodos que tengan el nombre indicado.
     */
    public void leerNodosPorNombre(String nodoNombre, Consumer<Nodo> nodoConsumer)
    {
        Nodo nodo = new Nodo();
        NodeList nodeList = doc.getElementsByTagName(nodoNombre);
        
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            // Hacerlo de tipo Element en lugar de Node nos da la ventaja de poder obtener los nodos hijo
            // a partir del nombre de su etiqueta
            nodo.elemento = (Element) nodeList.item(i);

            nodoConsumer.accept(nodo);
        }
    }

    public static class Nodo
    {
        public Element elemento;

        //region Constructores
        public Nodo(Element node)
        {
            this.elemento = node;
        }
        
        public Nodo() {}
        //endregion
        
        //region Métodos
        public String getTexto(String childName)
        {
            return elemento.getElementsByTagName(childName).item(0).getTextContent();
        }
        
        public int getTextoInt(String childName)
        {
            return Integer.parseInt(getTexto(childName));
        }
        
        public float getTextoFloat(String childName)
        {
            return Float.parseFloat(getTexto(childName));
        }
        
        public String getAtributo(String name)
        {
            return elemento.getAttribute(name);
        }
        //endregion
    }
}
