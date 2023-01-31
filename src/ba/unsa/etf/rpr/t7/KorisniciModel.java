package ba.unsa.etf.rpr.t7;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Scanner;

public class KorisniciModel {
    private ObservableList<Korisnik> korisnici = FXCollections.observableArrayList();
    private SimpleObjectProperty<Korisnik> trenutniKorisnik = new SimpleObjectProperty<>();
    private static Connection cnctn;
    private PreparedStatement sviKorisniciUpit, updateKorisnikaUpit, obrisiKorisnikaUpit;

    public KorisniciModel() {
        try {
            cnctn = DriverManager.getConnection("jdbc:sqlite:korisnici.db");
            sviKorisniciUpit = cnctn.prepareStatement("SELECT * FROM korisnik");
            updateKorisnikaUpit = cnctn.prepareStatement("UPDATE korisnik SET ime=?, prezime=?, email=?, username=?, password=? WHERE id=?");
            obrisiKorisnikaUpit = cnctn.prepareStatement("DELETE FROM korisnik WHERE id=?");
        } catch (SQLException e) {
            regenerisiBazu();
            try {
                sviKorisniciUpit = cnctn.prepareStatement("SELECT * FROM korisnik");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void napuni() {
        // Ako je lista već bila napunjena, praznimo je
        // Na taj način se metoda napuni() može pozivati više puta u testovima
        korisnici.clear();

        try {
            ResultSet rs = sviKorisniciUpit.executeQuery();
            while (rs.next()) {
                Korisnik k = new Korisnik(rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6));
                k.setId(rs.getInt(1));
                korisnici.add(k);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        trenutniKorisnik.set(null);
    }

    private void regenerisiBazu() {
        Scanner in = null;
        try {
            in = new Scanner(new FileInputStream("korisnik.db.sql"));
            String upit = "";
            while(in.hasNext()) {
                upit += in.nextLine();
                if(upit.charAt(upit.length() - 1) == ';') {
                    try {
                        Statement stmt = cnctn.createStatement();
                        stmt.execute(upit);
                        upit = "";
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void vratiNaDefault() {
        // Dodali smo metodu vratiNaDefault koja trenutno ne radi ništa, a kada prebacite Model na DAO onda
        // implementirajte ovu metodu
        // Razlog za ovo je da polazni testovi ne bi padali nakon što dodate bazu
        try {
            Statement statement = cnctn.createStatement();
            statement.execute("DELETE FROM korisnik");
            regenerisiBazu();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void diskonektuj() {
        try {
            cnctn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<Korisnik> getKorisnici() {
        return korisnici;
    }

    public void setKorisnici(ObservableList<Korisnik> korisnici) {
        this.korisnici = korisnici;
    }

    public Korisnik getTrenutniKorisnik() {
        return trenutniKorisnik.get();
    }

    public SimpleObjectProperty<Korisnik> trenutniKorisnikProperty() {
        return trenutniKorisnik;
    }

    public void setTrenutniKorisnik(Korisnik trenutniKorisnik) {
        if(this.trenutniKorisnik.get() != null) updateKorisnika(this.trenutniKorisnik.get());
        this.trenutniKorisnik.set(trenutniKorisnik);
    }

    public void setTrenutniKorisnik(int i) {
        this.trenutniKorisnik.set(korisnici.get(i));
    }

    public void updateKorisnika(Korisnik k) {
        try {
            updateKorisnikaUpit.setString(1,k.getIme());
            updateKorisnikaUpit.setString(2,k.getPrezime());
            updateKorisnikaUpit.setString(3,k.getEmail());
            updateKorisnikaUpit.setString(4,k.getUsername());
            updateKorisnikaUpit.setString(5,k.getPassword());
            updateKorisnikaUpit.setInt(6,k.getId());
            updateKorisnikaUpit.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteKorisnika() {
        try {
            obrisiKorisnikaUpit.setInt(1, this.trenutniKorisnik.getValue().getId());
            obrisiKorisnikaUpit.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void zapisiDatoteku(File file) {
        if(file != null) {
            try {
                PrintWriter izlaz = new PrintWriter(file);
                StringBuilder sb = new StringBuilder();
                for(Korisnik korisnik : korisnici) {
                    sb.append(korisnik.getUsername()).append(':').append(korisnik.getPassword()).
                            append(':').append(korisnik.getId()).append(':').
                            append(korisnik.getId()).append(':').append(korisnik.getIme()).
                            append(' ').append(korisnik.getPrezime()).append("::\n");
                }
                izlaz.print(sb);
                izlaz.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
