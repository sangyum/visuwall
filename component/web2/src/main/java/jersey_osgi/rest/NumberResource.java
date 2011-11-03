package jersey_osgi.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import net.awired.ajsl.web.service.JsService;
import org.osgi.service.http.HttpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Path("/")
@Scope("request")
public class NumberResource {

    @Autowired
    HttpService httpService;

    @Autowired
    JsService jsService;

    public NumberResource() {
        System.out.println("GENRE");
    }

    @GET
    @Produces("text/xml")
    public String listNumbers() {
        try {
            jsService.getJsLinks(null);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();

        sb.append("<!-- Guess the right number! The following numbers are available. -->");
        sb.append("\n");
        sb.append("<numbers>");
        sb.append("\n");
        for (Number number : Number.NUMBERS) {
            sb.append("<number>");
            sb.append(number.getNumber());
            sb.append("</number>");
            sb.append("\n");
        }
        sb.append("</numbers>");
        return sb.toString();
    }

    @GET
    @Path("/{id}")
    @Produces("text/plain")
    public String getNumber(@PathParam("id") String id) {
        try {
            int i = Integer.parseInt(id);
            Number n = Number.NUMBERS.get(i - 1);
            return n.toString();
        } catch (Throwable th) {
            return "Sorry that number is out of range";
        }
    }
}
