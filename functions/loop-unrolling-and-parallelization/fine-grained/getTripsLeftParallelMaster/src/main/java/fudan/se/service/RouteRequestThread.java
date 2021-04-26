package fudan.se.service;

import fudan.se.entity.RouteRequest;
import fudan.se.util.JsonUtils;
import fudan.se.util.Response;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import fudan.se.entity.Route;
import fudan.se.repository.RouteRepository;

public class RouteRequestThread implements Runnable {
    private OkHttpClient client = new OkHttpClient();
    private String routeId;
    private String startingPlaceId;
    private String endPlaceId;
    private String returnValRoute;
    private static final String WORKER_URL = System.getenv("WORKER_URL");

    RouteRequestThread(String routeId, String startingPlaceId, String endPlaceId) {
        this.routeId = routeId;
        this.startingPlaceId = startingPlaceId;
        this.endPlaceId = endPlaceId;
    }

    public void run() {
        String ret = "";

        RouteRequest routeRequest = new RouteRequest(this.routeId, this.startingPlaceId, this.endPlaceId);
        String json = JsonUtils.object2Json(routeRequest);

        try {
            RequestBody body = RequestBody.create(MediaType.parse("application/json"), json);
            Request request = new Request.Builder().
                                  url(WORKER_URL).
                                  post(body).build();

            okhttp3.Response response = client.newCall(request).execute();

            ret = response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //if (ret.length() > 100 ) {
        //    System.out.println(ret.substring(0,80));
        //} else {
        //    System.out.println(ret);
        //}

        Response<String> routeResponse = JsonUtils.json2Object(ret, Response.class);
        String routeId = JsonUtils.conveterObject(routeResponse.getData(),String.class);

        //System.out.println("Request Thread Result:" + route.getId());

        this.returnValRoute = routeId;
    }

    public String getReturnValRoute() {
        return this.returnValRoute;
    }
}
