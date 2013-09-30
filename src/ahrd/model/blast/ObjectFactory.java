//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.09.30 at 10:52:58 AM CEST 
//


package ahrd.model.blast;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ahrd.model package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ahrd.model
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Parameters }
     * 
     */
    public Parameters createParameters() {
        return new Parameters();
    }

    /**
     * Create an instance of {@link Statistics }
     * 
     */
    public Statistics createStatistics() {
        return new Statistics();
    }

    /**
     * Create an instance of {@link IterationHits }
     * 
     */
    public IterationHits createIterationHits() {
        return new IterationHits();
    }

    /**
     * Create an instance of {@link Hit }
     * 
     */
    public Hit createHit() {
        return new Hit();
    }

    /**
     * Create an instance of {@link BlastOutput }
     * 
     */
    public BlastOutput createBlastOutput() {
        return new BlastOutput();
    }

    /**
     * Create an instance of {@link BlastOutputParam }
     * 
     */
    public BlastOutputParam createBlastOutputParam() {
        return new BlastOutputParam();
    }

    /**
     * Create an instance of {@link BlastOutputIterations }
     * 
     */
    public BlastOutputIterations createBlastOutputIterations() {
        return new BlastOutputIterations();
    }

    /**
     * Create an instance of {@link BlastOutputMbstat }
     * 
     */
    public BlastOutputMbstat createBlastOutputMbstat() {
        return new BlastOutputMbstat();
    }

    /**
     * Create an instance of {@link IterationStat }
     * 
     */
    public IterationStat createIterationStat() {
        return new IterationStat();
    }

    /**
     * Create an instance of {@link Iteration }
     * 
     */
    public Iteration createIteration() {
        return new Iteration();
    }

    /**
     * Create an instance of {@link Hsp }
     * 
     */
    public Hsp createHsp() {
        return new Hsp();
    }

    /**
     * Create an instance of {@link HitHsps }
     * 
     */
    public HitHsps createHitHsps() {
        return new HitHsps();
    }

}
