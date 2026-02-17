package org.example.sentimentanalysis.controller;

import org.example.sentimentanalysis.dto.responseDto.TrainPanelDTO;
import org.example.sentimentanalysis.response.Response;
import org.example.sentimentanalysis.service.DomainsService;
import org.example.sentimentanalysis.service.TrainTasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TrainController {

    @Autowired
    private TrainTasksService trainTasksService;
    @Autowired
    private DomainsService domainsService;

    public Response<TrainPanelDTO> listTrainPanel() {
        TrainPanelDTO trainPanelDTO =domainsService.listTrainPanel();
        return Response.data(trainPanelDTO);
    }
}
