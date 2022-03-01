public class Circuit implements EscritorXML.ObjetoXML
{
    //region Atributos
    int id;
    String name;
    String location;
    String country;
    //endregion

    //region Constructores
    public Circuit(int id, String name, String location, String country)
    {
        this.id = id;
        this.name = name;
        this.location = location;
        this.country = country;
    }
    public Circuit() {}
    //endregion
    
    //region Métodos
    @Override public String toString()
    {
        return "Circuit{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
    //endregion
}
