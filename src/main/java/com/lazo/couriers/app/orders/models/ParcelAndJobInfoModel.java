package com.lazo.couriers.app.orders.models;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Lazo on 2021-05-07
 */

@Getter
@Setter
public class ParcelAndJobInfoModel {

    //ამანათები
    private String todayParcels;
    private String inActiveParcels;
    private String parcelsWithOutJob;
    private String madeParcels;
    private String activeParcels;
    private String allParcels;

    //შეკვეთები
    private String todayJobs;
    private String activeJobs;
    private String onHoldJobs;
    private String doneJobs;
    private String allJobs;


    public ParcelAndJobInfoModel(Long todayParcels, Long inActiveParcels, Long parcelsWithOutJob,
                                 Long madeParcels, Long activeParcels, Long allParcels,
                                 Long todayJobs, Long activeJobs, Long onHoldJobs, Long doneJobs, Long allJobs) {
    this.todayParcels = String.valueOf(todayParcels);
    this.inActiveParcels = String.valueOf(inActiveParcels);
    this.parcelsWithOutJob = String.valueOf(parcelsWithOutJob);
    this.madeParcels = String.valueOf(madeParcels);
    this.activeParcels = String.valueOf(activeParcels);
    this.allParcels = String.valueOf(allParcels);
    this.todayJobs = String.valueOf(todayJobs);
    this.activeJobs = String.valueOf(activeJobs);
    this.onHoldJobs = String.valueOf(onHoldJobs);
    this.doneJobs = String.valueOf(doneJobs);
    this.allJobs = String.valueOf(allJobs);
    }
}
