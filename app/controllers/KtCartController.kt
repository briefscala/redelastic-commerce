package controllers

import com.fasterxml.jackson.databind.JsonNode
import com.google.inject.Inject
import core.cart.api.CartItem
import core.cart.api.CartService
import core.product.api.ProductService
import javaslang.collection.List
import play.libs.Json
import play.mvc.Controller
import play.mvc.Result
import play.mvc.Results
import java.util.*
import java.util.concurrent.CompletionStage

class KtCartController @Inject constructor(
        val service : CartService,
        val productService : ProductService)
    : Controller() {

    fun getCart(userId: String): CompletionStage<Result> {
        return service.getCartContents(userId).thenApply { cart ->
            val items = cart.map {
                val product = productService.getProduct(it.productId)
                DisplayCartItem(
                    it.productId,
                    product.name,
                    product.description,
                    it.quantity,
                    it.price
                )
            }
            ok(Json.toJson(items))
        }
    }

    fun updateCart(): Result {
        val json = request().body().asJson()
        val userId = json.findValue("userId").asText()
        val nodes: MutableList<JsonNode> = json.findValues("items")
        val ls = List.ofAll(nodes) // the interface needs a different list type
        val cartItems = ls.flatMap {
            val prodId = it.get("productId").asText()
            if (prodId.isNotEmpty()) {
                List.of(CartItem(
                    UUID.fromString(it.get("productId").asText()),
                    it.get("quantity").asInt(),
                    it.get("price").asDouble()
                ))
            } else List.empty()
        }
        return if (userId.isNotEmpty() && !cartItems.isEmpty) {
            service.updateCartItems(userId, cartItems)
            ok()
        }  else notFound()
    }

    fun deleteCart(userId: String): Result {
        service.emptyCart(userId)
        return ok()
    }

    data class DisplayCartItem(val productId: UUID, val name: String, val description: String, val quantity: Int?, val price: Double?)
}