package org.mypetstore.service;

import org.mypetstore.domain.Category;
import org.mypetstore.domain.Item;
import org.mypetstore.domain.Product;
import org.mypetstore.persistence.CategoryDAO;
import org.mypetstore.persistence.ItemDAO;
import org.mypetstore.persistence.ProductDAO;
import org.mypetstore.persistence.impl.CategoryDAOImpl;
import org.mypetstore.persistence.impl.ItemDAOImpl;
import org.mypetstore.persistence.impl.ProductDAOImpl;

import java.util.List;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


/**
 * Created by Mr.Wan on 2016/3/18.
 */
public class CatalogService {

    private CategoryDAO categoryDAO;
    private ProductDAO productDAO;
    private ItemDAO itemDAO;

    public CatalogService() {
        categoryDAO = new CategoryDAOImpl();
        productDAO = new ProductDAOImpl();
        itemDAO = new ItemDAOImpl();
    }
	
	// Added for Compliance check test
	public Jedis redisConnection() {
                             GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
                             Jedis poolresource = null;
                             try (JedisPool pool = new JedisPool(genericObjectPoolConfig, getHost(), getPort(), timeout, getPassword())) {
                                           poolresource = pool.getResource();
                             } catch (Exception e) {
                                           logger.error(e.getMessage());
                             }
                             return poolresource;
    }

    public List<Category> getCategoryList() {
        return categoryDAO.getCategoryList();
    }

    public Category getCategory(String categoryId) {
        return categoryDAO.getCategory(categoryId);
    }

    public Product getProduct(String productId) {
        return productDAO.getProduct(productId);
    }

    public List<Product> getProductListByCategory(String categoryId) {
        return productDAO.getProductListByCategory(categoryId);
    }

    // TODO enable using more than one keyword
    public List<Product> searchProductList(String keyword) {
        return productDAO.searchProductList("%" + keyword.toLowerCase() + "%");
    }

    public List<Item> getItemListByProduct(String productId) {
        return itemDAO.getItemListByProduct(productId);
    }

    public Item getItem(String itemId) {
        return itemDAO.getItem(itemId);
    }

    public boolean isItemInStock(String itemId) {
        return itemDAO.getInventoryQuantity(itemId) > 0;
    }
}
