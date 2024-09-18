public class Lista {
    private String nazwa;
    private float cena;
    private int id;
    private int ilosc;


    public Lista(int id, String nazwa, float cena, int ilosc) {
        this.id= id;
        this.nazwa = nazwa;
        this.cena = cena;
        this.ilosc = ilosc;
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
    public int getIlosc() {
        return ilosc;
    }
}

