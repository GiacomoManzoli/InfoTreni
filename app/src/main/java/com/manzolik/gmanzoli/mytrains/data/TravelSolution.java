package com.manzolik.gmanzoli.mytrains.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TravelSolution implements JSONPopulable, Serializable{

    private String duration;
    private List<SolutionElement> elements;
    private String departureStationName;
    private String arrivalStaionName;

    public String getDepartureStationName() {
        return departureStationName;
    }

    public void setDepartureStationName(String departureStationName) {
        this.departureStationName = departureStationName;
    }

    public String getArrivalStaionName() {
        return arrivalStaionName;
    }

    public void setArrivalStaionName(String arrivalStaionName) {
        this.arrivalStaionName = arrivalStaionName;
    }

    /*
    * "soluzioni": [
            {
            "durata": "01:09",
            "vehicles": [
            {
            "origine": "Rovigo",
            "destinazione": "Venezia S.Lucia",
            "orarioPartenza": "2016-02-26T04:11:00",
            "orarioArrivo": "2016-02-26T05:20:00",
            "categoria": "214",
            "categoriaDescrizione": "ICN",
            "numeroTreno": "774"
            }
            ]
            },
    * */

    public String getDuration() {
        return duration;
    }

    public List<SolutionElement> getElements() {
        return elements;
    }

    @Override
    public void populate(JSONObject data) {
        duration = data.optString("durata");
        elements = new ArrayList<>();
        JSONArray elementsArray = data.optJSONArray("vehicles");
        for (int i = 0; i < elementsArray.length(); i++) {
            JSONObject elem = elementsArray.optJSONObject(i);
            SolutionElement e = new SolutionElement();
            e.populate(elem);
            elements.add(e);
        }
    }


    public class SolutionElement implements JSONPopulable, Serializable {

        private String departure;
        private String arrival;
        private Calendar departureTime;
        private Calendar arrivalTime;
        private String trainCode;
        private String category;


        public String getDeparture() {
            return departure;
        }
        public Calendar getDepartureTime() {
            return departureTime;
        }

        public String getArrival() {
            return arrival;
        }
        public Calendar getArrivalTime() {
            return arrivalTime;
        }

        public String getTrainCode() {
            return trainCode;
        }

        public String getCategory() {
            return category;
        }

        @Override
        public String toString() {
            SimpleDateFormat format = new SimpleDateFormat( "HH:mm", Locale.getDefault());
            return String.format("%s %s verso %s delle %s", category, trainCode, arrival, format.format(departureTime.getTime()));
        }


        @Override
        public int hashCode() {
            int result = departure != null ? departure.hashCode() : 0;
            result = 31 * result + (arrival != null ? arrival.hashCode() : 0);
            result = 31 * result + (departureTime != null ? departureTime.hashCode() : 0);
            result = 31 * result + (trainCode != null ? trainCode.hashCode() : 0);
            result = 31 * result + (category != null ? category.hashCode() : 0);
            return result;
        }

        @Override
        public void populate(JSONObject data) {
            departure = data.optString("origine");
            arrival = data.optString("destinazione");
            trainCode = data.optString("numeroTreno");
            category = data.optString("categoriaDescrizione");
            SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

            Date d = null;
            try {
                d = format.parse(data.optString("orarioPartenza"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            departureTime = Calendar.getInstance();
            departureTime.setTime(d);


            d = null;
            try {
                d = format.parse(data.optString("orarioArrivo"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            arrivalTime = Calendar.getInstance();
            arrivalTime.setTime(d);
        }

    }
}
