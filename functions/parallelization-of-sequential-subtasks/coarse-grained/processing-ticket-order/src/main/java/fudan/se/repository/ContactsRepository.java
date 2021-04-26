package fudan.se.repository;
import fudan.se.entity.Contacts;

import java.util.UUID;


public interface ContactsRepository{

    /**
     * find by id
     *
     * @param id id
     * @return Contacts
     */
    Contacts findById(UUID id);

}
