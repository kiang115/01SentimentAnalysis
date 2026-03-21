# Controller + Service 代码模板

## 1. Request DTO（XXRec）

```java
@Data
public class MerchantAddRec {
    @NotBlank(message = "name不能为空")
    private String name;

    @NotNull(message = "domainId不能为空")
    private Long domainId;

    private String description;
}
```

## 2. Response DTO（XXSend）

```java
@Data
@Builder
public class MerchantDetailSend {
    private Long merchantId;
    private String name;
    private String domainName;
    private String description;
}
```

## 3. Controller 模板

```java
@RestController
public class MerchantController {

    @Autowired
    private MerchantsService merchantsService;

    @PostMapping("/addMerchant")
    public Response<Void> addMerchant(@RequestBody @Valid MerchantAddRec rec) {
        merchantsService.addMerchant(rec);
        return Response.success();
    }
}
```

## 4. Service 接口模板

```java
public interface MerchantsService extends IService<Merchants> {
    void addMerchant(MerchantAddRec rec);
}
```

## 5. ServiceImpl 模板

```java
@Service
public class MerchantsServiceImpl extends ServiceImpl<MerchantsMapper, Merchants>
        implements MerchantsService {

    @Override
    public void addMerchant(MerchantAddRec rec) {
        boolean exists = this.exists(new LambdaQueryWrapper<Merchants>()
                .eq(Merchants::getName, rec.getName()));
        if (exists) {
            throw new CustomBusinessException("商家名称已存在");
        }

        Merchants entity = new Merchants();
        entity.setName(rec.getName());
        entity.setDomainId(rec.getDomainId());
        entity.setDescription(rec.getDescription());

        this.save(entity);
    }
}
```
