package fi.micronova.tkk.xray.xrdmodel;
import java.util.*;
import java.io.*;
import fi.micronova.tkk.xray.complex.*;


/** Scattering factor database.
 *
 * <p>
 *
 * This class imports atomic masses and scattering factors from ASCII files.
 * Atomic masses are imported from a single text file. Scattering factors are
 * loaded from a directory for every element for which an atomic mass is
 * specified in the atomic mass file. Information for an element is only
 * available if both the atomic mass and the scattering factors for the element
 * are available.
 *
 * <p>
 *
 * This class is thread safe since it is immutable. Lookups may be performed
 * simultaneously by multiple threads.
 *
 */

public class SFTables implements LookupTable {

    private class AtomicSymbols {
        public final Bijection<String,Integer> symbols;
        public AtomicSymbols(InputStream is) throws IOException, FileFormatException {
            Bijection<String,Integer> tempBijection = new HashBijection<String,Integer>();
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            try {
                String line;
                while((line = r.readLine()) != null) {
                    StringTokenizer t = new StringTokenizer(line);
                    String element = t.nextToken();
                    String Zs = t.nextToken();
                    int Z = Integer.parseInt(Zs);
                    if(Z <= 0)
                        throw new FileFormatException();
                    if(t.hasMoreElements())
                        throw new FileFormatException();
                    tempBijection.put(element,Z);
                }
                symbols = new UnmodifiableBijection<String,Integer>(tempBijection);
            }
            catch(NumberFormatException ex) {
                throw new FileFormatException();
            }
            catch(NoSuchElementException ex) {
                throw new FileFormatException();
            }
        }
    }

    /* helper class to read atomic values (double) */
    /* File format: every row contains atomic symbol and a double value separated by spaces */
    /* (or if symbols == null, atomic number instead of atomic symbol) */
    private class AtomicValues {
        public final Map<Integer,Double> valueMap;
        public AtomicValues(InputStream is, Bijection<String,Integer> symbols) throws IOException, FileFormatException {
            Map<Integer,Double> tempMap = new HashMap<Integer,Double>();
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            try {
                String line;
                while((line = r.readLine()) != null) {
                    StringTokenizer t = new StringTokenizer(line);
                    String element = t.nextToken();
                    String As = t.nextToken();
                    int Z;
                    double A = Double.parseDouble(As);

                    if(t.hasMoreElements())
                        throw new FileFormatException();
                    if(symbols == null) {
                        Z = Integer.parseInt(element);
                    } else {
                        if(!symbols.containsKey(element))
                            throw new FileFormatException();
                        Z = symbols.getByKey(element);
                    }
                    tempMap.put(Z,A);
                }
                valueMap = Collections.unmodifiableMap(tempMap);
            }
            catch(NumberFormatException ex) {
                throw new FileFormatException();
            }
            catch(NoSuchElementException ex) {
                throw new FileFormatException();
            }
        }
    }

    private class AtomicSFs {
        public final Map<Integer,ASF> asfMap;
        public AtomicSFs(InputStream is, Bijection<String,Integer> symbols) throws IOException, FileFormatException {
            Map<Integer,ASF> tempMap = new HashMap<Integer,ASF>();
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            try {
                String line;
                while((line = r.readLine()) != null) {
                    StringTokenizer t = new StringTokenizer(line);
                    String element = t.nextToken();
                    int Z;
                    List<Double> coeffs = new ArrayList<Double>();
                    List<ASFGaussian> gaussians = new ArrayList<ASFGaussian>();
                    while(t.hasMoreElements()) {
                        coeffs.add(Double.parseDouble(t.nextToken()));
                    }
                    if(symbols == null) {
                        Z = Integer.parseInt(element);
                    } else {
                        if(!symbols.containsKey(element))
                            throw new FileFormatException();
                        Z = symbols.getByKey(element);
                    }
                    if(coeffs.size()%2 != 1)
                        throw new FileFormatException();
                    for(int i=0; i+1<coeffs.size(); i+=2) {
                        double a = coeffs.get(i);
                        double b = coeffs.get(i+1);
                        gaussians.add(new ASFGaussian(a,b/1e20));
                    }
                    tempMap.put(Z,new ASF(gaussians,coeffs.get(coeffs.size()-1)));
                }
                asfMap = Collections.unmodifiableMap(tempMap);
            }
            catch(NumberFormatException ex) {
                throw new FileFormatException();
            }
            catch(NoSuchElementException ex) {
                throw new FileFormatException();
            }
        }
    }



    private final Bijection<String,Integer> symbols;
    private final Map<Integer,Atom> atoms;

    /** Constructor.
     * <p>
     * Creates the database and loads atomic masses and scattering factors.
     *
     * @param s a text file of atomic symbols
     * @param m a text file of atomic masses
     * @param b a text file of Debye B-coefficients
     * @param cm a text file of Cromer-Mann coefficients
     * @param d a directory which contains scattering factor files
     *
     * @throws FileFormatException if the file format of the atomic mass file or a scattering factor is invalid
     * @throws IOException if an I/O error occurs
     *
     */
    public SFTables(File s, File m, File b, File cm, File d) throws FileFormatException, IOException {
        Map<Integer,Double> masses;
        Map<Integer,Double> bFactors;
        Map<Integer,ASF> sfs;
        Map<Integer,Atom> atoms = new HashMap<Integer,Atom>();

        this.symbols = new AtomicSymbols(new FileInputStream(s)).symbols;
        masses = new AtomicValues(new FileInputStream(m), symbols).valueMap;
        bFactors = new AtomicValues(new FileInputStream(b), null).valueMap;
        sfs = new AtomicSFs(new FileInputStream(cm), null).asfMap;
         
        for(Map.Entry<String,Integer> e: symbols.entrySet()) {
            int Z = e.getValue();
            File f;
            SortedMap<Double,Complex> fMap;
            double mass, B;
            ASF asf;
            InputStream is = null;

            if(!(masses.containsKey(Z) && sfs.containsKey(Z) && bFactors.containsKey(Z)))
                continue;
            mass = masses.get(Z);
            asf = sfs.get(Z);
            B = bFactors.get(Z);

            f = new File(d,e.getKey().toLowerCase()+".nff");
            try {
                is = new FileInputStream(f);
            }
            catch(IOException ex) {}
            if(is == null)
                continue;
            fMap = readHenke(is);
            atoms.put(Z, new TableAtom(Z, mass, B, asf, fMap));
        }

        this.atoms = Collections.unmodifiableMap(atoms);
    }
    private static SortedMap<Double,Complex> readHenke(InputStream is) throws IOException, FileFormatException {
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        if(r.readLine() == null)
            throw new FileFormatException();
        try {
            SortedMap<Double,Complex> data = new TreeMap<Double,Complex>();
            String line;
            while((line = r.readLine()) != null) {
                StringTokenizer t = new StringTokenizer(line);
                String Es = t.nextToken();
                String f1s = t.nextToken();
                String f2s = t.nextToken();
                double E = Double.parseDouble(Es);
                double f1 = Double.parseDouble(f1s);
                double f2 = Double.parseDouble(f2s);
                if(f1 != -9999)
                    data.put(E,new Complex(f1,f2));
                if(t.hasMoreElements())
                    throw new FileFormatException();
            }
            return Collections.unmodifiableSortedMap(data);
        }
        catch(NumberFormatException ex) {
            throw new FileFormatException();
        }
        catch(NoSuchElementException ex) {
            throw new FileFormatException();
        }
    }
    public Atom lookup(int Z) throws ElementNotFound {
        Atom atom = atoms.get(Z);
        if(atom == null)
            throw new ElementNotFound("Element "+Z+" not found");
        return atom;
    }
    public static SFTables defaultLookup() throws FileFormatException, IOException {
        File s, m, b, cm, d;
        s = new File("atomic_symbols.txt");
        m = new File("atomic_masses.txt");
        b = new File("atomic_B.txt");
        cm = new File("atomic_sf.txt");
        d = new File("henke");
        return new SFTables(s, m, b, cm, d);
    }

    /* OK */
    public static void main(String[] args) {
        try {
            File s, m, b, cm, d;
            Atom atom;

            s = new File("atomic_symbols.txt");
            m = new File("atomic_masses.txt");
            b = new File("atomic_B.txt");
            cm = new File("atomic_sf.txt");
            d = new File("henke");
            SFTables tab = new SFTables(s, m, b, cm, d);


            System.out.println("tables");
            atom = tab.lookup(31);
            System.out.println(atom.getZ());
            System.out.println(atom.getMass());
            System.out.println(atom.bFactor());
            System.out.println(atom.asf().calc(0));
            System.out.println(atom.asf().calc(0.3e10));
            System.out.println(atom.hoenl(1.54056e-10));
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
