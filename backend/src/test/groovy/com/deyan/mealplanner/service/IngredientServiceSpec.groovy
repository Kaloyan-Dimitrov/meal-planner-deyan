//package com.deyan.mealplanner.service
//import com.deyan.mealplanner.AbstractIT
//import com.deyan.mealplanner.dto.IngredientDTO
//import com.deyan.mealplanner.dto.NewIngredientDTO
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.context.ApplicationContext;
//class IngredientServiceSpec extends AbstractIT{
//
//    @Autowired ApplicationContext ctx
//    @Autowired
//    IngredientService service
//
//    def "CRUD path"(){
//        //Create the Ingredient
//        given:
//        def newDto = new NewIngredientDTO('Brown Rice','Grains', 360G as BigDecimal)
//
//        when:
//        def saved = service.createIngredient(newDto)
//
//        then:
//        saved.id()!=null
//        saved.name()=='Brown Rice'
//        //Update the Ingredient
//        when:
//        def updated = service.update(saved.id(),new IngredientDTO(saved.id()
//                ,'Whole Grain Brown Rice','Grains',360G as BigDecimal))
//
//        then:
//        updated.name()=='Whole Grain Brown Rice'
//        updated.id()==saved.id()
//        //Delete the Ingredient
//        when:
//        service.delete(saved.id())
//
//        then:
//        service.getAllIngredients().stream().noneMatch {it.id()==saved.id()}
//    }
//    def "duplicate ingredient"(){
//        given:
//        service.createIngredient(new NewIngredientDTO('Olive Oil', 'Fats', 884G as BigDecimal))
//
//        when:
//        service.createIngredient(new NewIngredientDTO('Olive Oil', 'Fats', 884G as BigDecimal))
//
//        then:
//        thrown(IllegalArgumentException)
//    }
//    def "spring context sanity"() {
//        expect:
//        assert ctx != null                           // should pass
//        assert ctx.containsBean("ingredientService") // true
//        assert service != null                       // no more NPE
//    }
//
//}
