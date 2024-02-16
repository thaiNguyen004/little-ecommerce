package thainguyen;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import thainguyen.data.*;
import thainguyen.domain.*;
import thainguyen.domain.valuetypes.Price;
import thainguyen.domain.valuetypes.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

@Configuration
public class Develop {

    /*@Bean
    @Transactional*/
    public CommandLineRunner data (BrandRepository brandRepo,
                                   CategoryRepository categoryRepo,
                                   ProductRepository productRepo,
                                   PasswordEncoder encoder,
                                   UserRepository userRepo,
                                   SizeRepository sizeRepo,
                                   DetailProductRepository detailProductRepo,
                                   PaymentRepository paymentRepo,
                                   DiscountRepository discountRepo,
                                   OrderRepository orderRepo,
                                   LineItemRepository lineItemRepo) {
        return args -> {
            User admin = new User("admin", encoder.encode("password"), "email@example1.com"
                    , "Nguyen admin", "male", 19, "link avatar", User.Position.ADMIN);
            Address adminAddress = new Address("0336514962", "Phú Thọ", "Hạ Hòa", "Yên Kỳ", "Khu 14");
            admin.getAddresses().add(adminAddress);
            userRepo.save(admin);
            User employee = new User("employee", encoder.encode("password"), "nguyenntph33935@fpt.edu.com"
                    , "Nguyen employee", "male", 19, "link avatar", User.Position.EMPLOYEE);
            userRepo.save(employee);
            User customer1 = new User("customer", encoder.encode("password"), "email@example2.com"
                    , "Nguyen customer", "female", 19, "link avatar", User.Position.CUSTOMER);
            userRepo.save(customer1);
            User customer2 = new User("customer2", encoder.encode("password"), "email@example3.com"
                    , "Nguyen customer2", "male", 23, "link avatar", User.Position.CUSTOMER);
            userRepo.save(customer2);

            Brand brand1 = new Brand("gucci", "link picture of gucci");
            Brand brand2 = new Brand("louis vuitton", "link picture of louis vuitton");

            brandRepo.save(brand1);
            brandRepo.save(brand2);

            Category category1 = new Category("shirt", "link picture of shirt",
                    "description of shirt", null);
            Category categoryMerge = categoryRepo.save(category1);
            Category category2 = new Category("pants", "link picture of pants",
                    "description of pants", null);
            category2 = categoryRepo.save(category2);
            Category category3 = new Category("short pants", "link picture of short pants",
                    "description of shirt", category1);
            category3 = categoryRepo.save(category3);

            Product product1 = new Product(category1, brand1, "Gucci shirt",
                    "picture of product 1", "Description of Gucci shirt");
            productRepo.save(product1);

            Product product2 = new Product(category2, brand1, "Pants shirt",
                    "picture of product 2", "Description of Gucci pants");
            productRepo.save(product2);

            Product product3 = new Product(category1, brand2, "Louis Vuitton shirt",
                    "picture of product 3", "Description of Louis Vuitton shirt");
            productRepo.save(product3);

            Product product4 = new Product(category2, brand2, "Louis Vuitton pants",
                    "picture of product 4", "Description of Louis Vuitton shirt");
            productRepo.save(product4);

            Size sizeM = new Size(Size.Name.M, 15, 20, 10);
            sizeM.setCategory(category1);
            sizeM.setBrand(brand1);
            sizeRepo.save(sizeM);

            Size sizeS = new Size(Size.Name.S, 6, 10, 5);
            sizeS.setCategory(category2);
            sizeS.setBrand(brand1);
            sizeRepo.save(sizeS);

            DetailProduct detailProduct1 = new DetailProduct(1.1, sizeS
                    , 60000);
            detailProduct1.setProduct(product3);
            detailProduct1.setWeight(1.1);
            detailProductRepo.save(detailProduct1);


            DetailProduct detailProduct2 = new DetailProduct(0.5, sizeS
                    , 120000);
            detailProduct2.setProduct(product1);
            detailProductRepo.save(detailProduct2);


            DetailProduct detailProduct3 = new DetailProduct(1222.25, sizeM,
                    15555);
            detailProduct3.setProduct(product2);
            detailProductRepo.save(detailProduct3);

            Discount discount = new Discount("HAPPYNEWYEAR2024"
                    , Discount.Type.PERCENTAGE
                    , Discount.Kind.NORMAL
                    , 15
                    , 100
                    , LocalDateTime.of(2024, 01, 25, 00, 00, 00)
                    , LocalDateTime.of(2024, 02, 25, 00, 00, 00)
                    );

            Discount discount2 = new Discount("FREESHIP2024"
                    , Discount.Type.PERCENTAGE
                    , Discount.Kind.FREESHIP
                    , 15
                    , 100
                    , LocalDateTime.of(2024, 01, 25, 00, 00, 00)
                    , LocalDateTime.of(2024, 02, 25, 00, 00, 00));

            LineItem lineItem1 = new LineItem(detailProduct1, 2);
            LineItem lineItem2 = new LineItem(detailProduct2, 3);

            Order order1 = new Order();
            order1.addDiscount(discount);
            order1.addDiscount(discount2);
            order1.setStatus(Status.SUCCESS);
            order1.setUser(customer1);
            order1.setAddress(
                    new Address("0938473771"
                            , "Hà Nội"
                            , "Bắc Từ Liêm"
                            , "Cầu Diễn"
                            , "Số 12"));
            order1.addLineItem(lineItem1);
            order1.addLineItem(lineItem2);
            orderRepo.save(order1);

            Payment payment = new Payment();
            payment.setAmount(new Price(BigDecimal.valueOf(100000), Currency.getInstance("VND")));
            payment.setPaymentMethod(Payment.PaymentMethod.CAST);
            payment.setStatus(Status.SUCCESS);
            payment.setOrder(order1);
            paymentRepo.save(payment);

        };
    }

}
