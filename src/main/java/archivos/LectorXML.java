package archivos;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.function.Consumer;

public class LectorXML
{
    String ficheroXML;
    Document doc;

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
        public String getString(String childName)
        {
            return elemento.getElementsByTagName(childName).item(0).getTextContent();
        }

        public int getInt(String childName)
        {
            return Integer.parseInt(getString(childName));
        }

        public float getFloat(String childName)
        {
            return Float.parseFloat(getString(childName));
        }

        public String getAtributo(String name)
        {
            return elemento.getAttribute(name);
        }
        //endregion
    }

    public LectorXML(Document doc)
    {
        this.doc = doc;
        this.ficheroXML = doc.getDocumentURI();
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
}
