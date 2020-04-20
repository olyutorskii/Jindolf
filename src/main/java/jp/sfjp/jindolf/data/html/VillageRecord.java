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
class VillageRecord implements Comparable<VillageRecord> {

    private final String villageId;
    private final String fullVillageName;
    private final VillageState villageStatus;

    private final int villageIdNum;


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

        this.villageIdNum = Integer.parseInt(villageId);

        return;
    }

    /**
     * return village id on CGI query.
     *
     * @return village id
     */
    String getVillageId(){
        return this.villageId;
    }

    /**
     * return long village name.
     *
     * @return long village name
     */
    String getFullVillageName(){
        return this.fullVillageName;
    }

    /**
     * return village status.
     *
     * @return village status
     */
    VillageState getVillageStatus(){
        return this.villageStatus;
    }

    /**
     * {@inheritDoc}
     *
     * <p>村IDの自然数順に順序づける。
     *
     * @param rec {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int compareTo(VillageRecord rec) {
        int result = this.villageIdNum - rec.villageIdNum;
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.villageId.hashCode();
    }

    /**
     * {@inheritDoc}
     *
     * @param obj {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null) return false;

        if(! (obj instanceof VillageRecord)) return false;
        VillageRecord other = (VillageRecord) obj;

        return this.villageId.equals(other.villageId);
    }

}
