/*
 * village record
 *
 * License : The MIT License
 * Copyright(c) 2020 olyutorskii
 */

package jp.sfjp.jindolf.data.html;

import jp.sourceforge.jindolf.corelib.VillageState;

/**
 * Village record on HTML.
 */
public class VillageRecord {

    private final String villageId;
    private final String fullVillageName;
    private final VillageState villageStatus;

    /**
     * Constructor.
     *
     * @param villageId village id on CGI query
     * @param fullVillageName full village name
     * @param villageStatus village status
     */
    VillageRecord(String villageId,
                  String fullVillageName,
                  VillageState villageStatus ){
        super();

        this.villageId = villageId;
        this.fullVillageName = fullVillageName;
        this.villageStatus = villageStatus;

        return;
    }

    /**
     * return village id on CGI query.
     *
     * @return village id
     */
    public String getVillageId(){
        return this.villageId;
    }

    /**
     * return long village name.
     *
     * @return long village name
     */
    public String getFullVillageName(){
        return this.fullVillageName;
    }

    /**
     * return village status.
     *
     * @return village status
     */
    public VillageState getVillageStatus(){
        return this.villageStatus;
    }

}
