package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception
    {

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db
        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).get();
        String[] route = train.getRoute().split(".");
        Map<String, Integer> routeMap = new HashMap<>();
        for(int i=0; i<route.length; i++)
        {
            routeMap.put(route[i], i);
        }
        if(routeMap.containsKey(bookTicketEntryDto.getFromStation().toString()) &&
                routeMap.containsKey(bookTicketEntryDto.getToStation().toString()) &&
                    routeMap.get(bookTicketEntryDto.getToStation().toString())-routeMap.get(bookTicketEntryDto.getFromStation().toString()) > 0)
        {
            List<Ticket> tickets = train.getBookedTickets();
            for(Ticket ticket : tickets)
            {
                String fro = ticket.getFromStation().toString();
                String to = ticket.getToStation().toString();
                String sorc = bookTicketEntryDto.getFromStation().toString();
                String dest = bookTicketEntryDto.getToStation().toString();

                if(routeMap.get(fro)<= routeMap.get(sorc) && routeMap.get(dest)<= routeMap.get(to))
                {
                    if(bookTicketEntryDto.getNoOfSeats()<= (train.getNoOfSeats()-ticket.getPassengersList().size()))
                    {
                        //book ticket
                        Ticket ticket1 = new Ticket();

                        //setting ticket parameters
                        List<Passenger> passengerList = new ArrayList<>();
                        for(Integer id : bookTicketEntryDto.getPassengerIds())
                        {
                            passengerList.add(passengerRepository.findById(id).get());
                        }

                        ticket1.setPassengersList(passengerList);
                        ticket1.setTotalFare((routeMap.get(dest) - routeMap.get(sorc))*300);
                        ticket1.setFromStation(bookTicketEntryDto.getFromStation());
                        ticket1.setToStation(bookTicketEntryDto.getToStation());
                        ticket1.setTrain(train);

                        passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get().getBookedTickets().add(ticket1);

                        train.getBookedTickets().add(ticket1);

                        trainRepository.save(train);
                        return ticket.getTicketId();
                    }
                    else
                    {
                        throw new Exception("Less tickets are available");
                    }
                }
            }
        }
        else
        {
            throw new Exception("Invalid stations");
        }
       return null;

    }
}
