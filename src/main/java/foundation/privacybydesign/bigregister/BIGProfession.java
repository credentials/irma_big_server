package foundation.privacybydesign.bigregister;

import java.math.BigDecimal;
import java.util.*;

/**
 * This class wraps a single profession from a ListHcpApprox4 result.
 * See BIGService.getProfessions for details.
 */
public class BIGProfession {
    private BigDecimal number;
    private String name;
    private GregorianCalendar startDate, endDate;
    private List<String> specialisms;

    public BIGProfession(BigDecimal number, String name, GregorianCalendar startDate, GregorianCalendar endDate) {
        this.number = number;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.specialisms = new ArrayList<>();
    }

    // Return the name of this profession
    public String getName() { return name; }

    // Only to be used by BIGService.getProfessions.
    // Add a single specialism.
    public void addSpecialism(String specialism) { specialisms.add(specialism); }

    // The BIG number. This number is part of a registration and not bound to a person.
    // One person with multiple professions has multiple BIG numbers: one per profession.
    public BigDecimal getRegistrationNumber() {
        return number;
    }

    // List of 0 or more specialisms associated with this profession.
    // Specialisms don't have a separate start or end date.
    public List<String> getSpecialisms() { return specialisms; }

    // Start date of this profession - when the registration took place.
    public GregorianCalendar getStartDate() { return startDate; }

    // End date of this profession. May be null if it's not yet determined and may lie in the past if this registration
    // isn't active anymore.
    public GregorianCalendar getEndDate() { return endDate; }
}
