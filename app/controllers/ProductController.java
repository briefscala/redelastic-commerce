package controllers;

import com.google.inject.Inject;
import akka.actor.ActorSystem;
import core.product.api.Product;
import core.product.api.ProductService;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import scala.concurrent.ExecutionContextExecutor;

import java.util.List;
import java.util.UUID;

public class ProductController extends Controller {

    private final ActorSystem actorSystem;
    private final ExecutionContextExecutor ec;
    private final ProductService ps;

    @Inject
    public ProductController(ActorSystem actorSystem, ExecutionContextExecutor ec, ProductService ps) {
        this.actorSystem = actorSystem;
        this.ec = ec;
        this.ps = ps;
    }

    public Result getProducts() {
        List<Product> products = ps.lookupProducts();
        return ok(Json.toJson(products));
    }

    public Result getProduct(String id) {
        return ok(Json.toJson(ps.getProduct(UUID.fromString(id))));
    }
}


