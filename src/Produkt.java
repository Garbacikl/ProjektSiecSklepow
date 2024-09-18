public class Produkt {
    private String nazwa;
    private float cena;
    private int id;

    public Produkt(int id, String nazwa, float cena) {
        this.id= id;
        this.nazwa = nazwa;
        this.cena = cena;
    }
    public int getId() {
        return id;
    }
    public String getNazwa() {
        return nazwa;
    }

    public float getCena() {
        return cena;
    }
}
