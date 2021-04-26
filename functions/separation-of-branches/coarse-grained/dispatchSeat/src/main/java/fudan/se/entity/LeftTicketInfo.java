package fudan.se.entity;

import java.util.Set;


public class LeftTicketInfo {

    private Set<Ticket> soldTickets;

    public LeftTicketInfo(){
        //Default Constructor
    }

    public Set<Ticket> getSoldTickets() {
        return soldTickets;
    }

    public void setSoldTickets(Set<Ticket> soldTickets) {
        this.soldTickets = soldTickets;
    }

    @Override
    public String toString() {
        return "LeftTicketInfo{" +
                "soldTickets=" + soldTickets +
                '}';
    }
}
