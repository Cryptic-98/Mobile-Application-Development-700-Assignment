package com.example.smartpantrymanager.logic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


















public final class IngredientSynonyms
{

    private static final List<String> PREPARATION_PREFIXES = Arrays.asList(
            "freshly", "fresh", "dried", "ground", "chopped", "sliced", "diced", "minced",
            "large", "small", "medium", "ripe", "raw", "cooked", "uncooked", "whole", "baby",
            "boneless", "skinless", "lean", "extra", "virgin", "organic", "free range",
            "roasted", "grilled", "boiled", "steamed", "frozen", "canned", "tinned", "jarred",
            "toasted", "crushed", "shredded", "cubed", "peeled", "seedless", "unsalted",
            "low fat", "reduced fat", "full fat", "half fat");

    private static final String[][] SYNONYM_GROUPS = {

            {"tomato", "tomatoes", "roma tomatoes", "plum tomatoes", "cherry tomatoes",
                    "chopped tomatoes", "tinned tomatoes", "canned tomatoes", "plum tomato",
                    "beefsteak tomato"},
            {"tomato puree", "tomato paste", "tomato pur\u00e9e"},
            {"onion", "onions", "red onion", "brown onion", "yellow onion", "white onion",
                    "shallot", "shallots"},
            {"spring onion", "spring onions", "scallion", "scallions", "green onion",
                    "green onions"},
            {"garlic", "garlic clove", "garlic cloves"},
            {"potato", "potatoes", "baby potatoes", "new potatoes"},
            {"sweet potato", "sweet potatoes"},
            {"carrot", "carrots"},
            {"bell pepper", "bell peppers", "capsicum", "capsicums", "red pepper",
                    "green pepper", "yellow pepper"},
            {"chili", "chili pepper", "chili peppers", "chilli", "chilli pepper",
                    "chilli peppers", "red chili", "green chili", "jalapeno", "jalapenos",
                    "birds eye chili"},
            {"chilli flakes", "chili flakes", "red pepper flakes", "crushed chilli"},
            {"cucumber", "cucumbers"},
            {"courgette", "zucchini", "courgettes", "zucchinis"},
            {"aubergine", "eggplant", "eggplants", "aubergines"},
            {"spinach", "baby spinach"},
            {"lettuce", "lettuces", "iceberg lettuce", "romaine lettuce"},
            {"cabbage", "cabbages", "white cabbage", "red cabbage"},
            {"broccoli"},
            {"cauliflower"},
            {"peas", "pea", "garden peas", "frozen peas"},
            {"green beans", "string beans", "runner beans"},
            {"asparagus"},
            {"leek", "leeks"},
            {"celery", "celery stalks", "celery stick"},
            {"mushroom", "mushrooms", "chestnut mushrooms", "button mushrooms"},
            {"ginger", "ground ginger"},
            {"coriander", "cilantro", "coriander leaves"},
            {"parsley", "parsley leaves"},
            {"basil", "basil leaves"},
            {"thyme"},
            {"rosemary"},
            {"mint"},
            {"corn", "sweetcorn", "sweet corn", "corn kernels", "tinned corn"},
            {"olive", "olives", "green olives", "black olives", "kalamata olives"},
            {"avocado", "avocados"},
            {"lemon", "lemons", "lemon juice"},
            {"lime", "limes", "lime juice"},
            {"orange", "oranges", "orange juice"},
            {"apple", "apples"},
            {"banana", "bananas"},
            {"mixed salad leaves", "salad leaves", "mixed leaves"},


            {"chicken", "chicken breast", "chicken breasts", "chicken thigh", "chicken thighs",
                    "chicken leg", "chicken legs", "chicken wing", "chicken wings",
                    "ground chicken", "minced chicken", "boneless chicken breasts"},
            {"beef", "ground beef", "minced beef", "beef steak", "steak", "beef steaks",
                    "beef mince"},
            {"pork", "ground pork", "minced pork", "pork chops", "pork mince"},
            {"lamb", "lamb mince", "ground lamb"},
            {"turkey", "ground turkey", "turkey mince", "turkey breast"},
            {"bacon", "bacon rashers", "streaky bacon"},
            {"sausage", "sausages", "sausage meat"},
            {"ham", "ham slices", "sliced ham"},
            {"chorizo", "chorizo slices"},
            {"fish", "white fish", "cod", "haddock", "salmon", "tuna", "fish fillet",
                    "fish fillets"},
            {"prawn", "prawns", "shrimp", "shrimps"},
            {"egg", "eggs", "large eggs", "free range eggs", "egg yolk", "egg yolks",
                    "egg white", "egg whites"},
            {"tofu", "firm tofu", "silken tofu"},
            {"tempeh"},


            {"milk", "whole milk", "skimmed milk", "skim milk", "semi skimmed milk",
                    "buttermilk"},
            {"butter", "unsalted butter", "salted butter"},
            {"cheese", "cheddar", "cheddar cheese", "grated cheese", "hard cheese"},
            {"parmesan", "parmesan cheese", "grated parmesan", "parmigiano reggiano"},
            {"mozzarella", "mozzarella cheese"},
            {"feta", "feta cheese"},
            {"cream", "double cream", "heavy cream", "single cream", "sour cream",
                    "creme fraiche", "whipping cream"},
            {"yogurt", "yoghurt", "natural yogurt", "greek yogurt"},


            {"flour", "plain flour", "all purpose flour", "all-purpose flour",
                    "wheat flour", "bread flour", "strong white flour"},
            {"self raising flour", "self-raising flour"},
            {"sugar", "white sugar", "caster sugar", "granulated sugar", "icing sugar",
                    "powdered sugar"},
            {"brown sugar", "light brown sugar", "dark brown sugar", "muscovado sugar"},
            {"salt", "table salt", "sea salt", "kosher salt", "fine salt", "coarse salt"},
            {"black pepper", "pepper", "ground black pepper", "white pepper", "peppercorns"},
            {"olive oil", "extra virgin olive oil", "extra-virgin olive oil", "virgin olive oil"},
            {"vegetable oil", "sunflower oil", "canola oil", "rapeseed oil", "neutral oil",
                    "cooking oil"},
            {"rice", "white rice", "brown rice", "basmati rice", "long grain rice",
                    "short grain rice"},
            {"pasta", "spaghetti", "penne", "linguine", "macaroni", "fusilli", "rigatoni",
                    "noodles", "egg noodles", "pasta shells", "penne pasta", "spaghetti pasta",
                    "macaroni pasta", "rigatoni pasta", "linguine pasta", "fusilli pasta",
                    "farfalle pasta"},
            {"bread", "white bread", "brown bread", "breadcrumbs", "bread crumbs",
                    "panko breadcrumbs"},
            {"baking powder"},
            {"baking soda", "bicarbonate of soda", "bicarb"},
            {"cornflour", "cornstarch", "corn starch"},
            {"cocoa powder", "cocoa", "unsweetened cocoa"},
            {"chocolate", "dark chocolate", "chocolate chips", "chocolate chunks",
                    "milk chocolate", "plain chocolate"},
            {"vanilla extract", "vanilla essence"},
            {"honey"},
            {"peanut butter"},
            {"jam", "strawberry jam", "apricot jam", "fruit jam"},
            {"yeast", "dried yeast", "instant yeast"},
            {"tortilla", "tortillas", "flour tortilla", "flour tortillas"},
            {"couscous"},
            {"quinoa"},
            {"oats", "rolled oats", "porridge oats", "oatmeal"},
            {"raisins", "sultanas", "currants"},
            {"coconut", "desiccated coconut", "coconut flakes"},
            {"coconut milk"},
            {"coconut oil"},
            {"chickpeas", "garbanzo beans", "tinned chickpeas"},
            {"kidney beans", "red kidney beans", "tinned kidney beans"},
            {"black beans", "tinned black beans"},
            {"lentils", "red lentils", "green lentils", "brown lentils", "puy lentils"},
            {"butter beans", "tinned butter beans"},
            {"stock", "vegetable stock", "chicken stock", "beef stock", "beef bouillon",
                    "broth", "chicken broth", "vegetable broth", "stock cube", "stock cubes"},
            {"soy sauce", "soya sauce", "tamari", "shoyu"},
            {"vinegar", "white vinegar", "cider vinegar", "balsamic vinegar", "rice vinegar",
                    "apple cider vinegar"},
            {"mustard", "dijon mustard", "wholegrain mustard", "honey mustard"},
            {"mayonnaise", "mayo"},
            {"ketchup", "tomato ketchup"},
            {"worcestershire sauce"},
            {"tabasco sauce", "hot sauce", "chilli sauce"},
            {"curry powder", "curry paste", "garam masala", "tikka masala", "biryani masala"},
            {"cumin", "ground cumin"},
            {"paprika", "smoked paprika"},
            {"turmeric"},
            {"cinnamon", "ground cinnamon"},
            {"oregano", "dried oregano"},
            {"bay leaf", "bay leaves"},
            {"peanut oil"},
            {"sesame oil"},
            {"water", "boiling water", "cold water", "warm water"}
    };

    private static final Map<String, List<String>> GROUP_INDEX = buildIndex();

    private IngredientSynonyms()
    {
    }











    public static List<String> expand(String name)
    {
        List<String> result = new ArrayList<>();
        if (name == null)
        {
            return result;
        }
        String base = normalize(name);
        if (base.isEmpty())
        {
            return result;
        }
        Set<String> visited = new LinkedHashSet<>();
        Deque<String> pending = new ArrayDeque<>();
        pending.add(base);
        while (!pending.isEmpty())
        {
            String current = pending.poll();
            if (!visited.add(current))
            {
                continue;
            }
            result.add(current);
            for (String candidate : neighbours(current))
            {
                if (!visited.contains(candidate))
                {
                    pending.add(candidate);
                }
            }
        }
        return result;
    }





    public static boolean hasSynonyms(String name)
    {
        return !groupMembers(normalize(name)).isEmpty();
    }











    public static String canonicalTerm(String name)
    {
        String normalized = normalize(name);
        if (normalized.isEmpty())
        {
            return normalized;
        }
        List<String> direct = GROUP_INDEX.get(normalized);
        if (direct != null && !direct.isEmpty())
        {
            return direct.get(0);
        }
        for (String candidate : expand(normalized))
        {
            List<String> members = GROUP_INDEX.get(candidate);
            if (members != null && !members.isEmpty())
            {
                return members.get(0);
            }
        }
        return normalized;
    }





    public static String normalize(String name)
    {
        if (name == null)
        {
            return "";
        }
        return name.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private static List<String> neighbours(String term)
    {
        List<String> result = new ArrayList<>();
        addIfPresent(result, singular(term));
        addIfPresent(result, plural(term));
        result.addAll(stripPreparationPrefixes(term));
        for (String member : groupMembers(term))
        {
            addIfPresent(result, member);
            addIfPresent(result, singular(member));
            addIfPresent(result, plural(member));
        }
        return result;
    }

    private static List<String> groupMembers(String term)
    {
        List<String> members = GROUP_INDEX.get(term);
        return members == null ? new ArrayList<>() : members;
    }

    private static void addIfPresent(List<String> target, String value)
    {
        if (value != null && !value.isEmpty() && !target.contains(value))
        {
            target.add(value);
        }
    }

    private static List<String> stripPreparationPrefixes(String term)
    {
        List<String> result = new ArrayList<>();
        String current = term;
        for (int depth = 0; depth < 3; depth++)
        {
            String stripped = stripSinglePrefix(current);
            if (stripped == null)
            {
                break;
            }
            result.add(stripped);
            current = stripped;
        }
        return result;
    }

    private static String stripSinglePrefix(String term)
    {
        for (String prefix : PREPARATION_PREFIXES)
        {
            if (term.length() > prefix.length() && term.startsWith(prefix + " "))
            {
                return term.substring(prefix.length() + 1);
            }
        }
        return null;
    }

    private static String singular(String term)
    {
        if (term.length() > 4 && term.endsWith("ies"))
        {
            return term.substring(0, term.length() - 3) + "y";
        }
        if (term.length() > 4 && term.endsWith("oes"))
        {
            return term.substring(0, term.length() - 3) + "o";
        }
        if (term.length() > 4 && (term.endsWith("ses") || term.endsWith("xes")
                || term.endsWith("zes") || term.endsWith("ches") || term.endsWith("shes")))
        {
            return term.substring(0, term.length() - 2);
        }
        if (term.length() > 3 && term.endsWith("s") && !term.endsWith("ss"))
        {
            return term.substring(0, term.length() - 1);
        }
        return null;
    }

    private static String plural(String term)
    {
        if (term.isEmpty() || term.endsWith("s"))
        {
            return null;
        }
        if (term.endsWith("y") && term.length() > 2 && !isVowel(term.charAt(term.length() - 2)))
        {
            return term.substring(0, term.length() - 1) + "ies";
        }
        if (term.endsWith("x") || term.endsWith("z") || term.endsWith("ch") || term.endsWith("sh"))
        {
            return term + "es";
        }
        return term + "s";
    }

    private static boolean isVowel(char character)
    {
        return "aeiou".indexOf(character) >= 0;
    }

    private static Map<String, List<String>> buildIndex()
    {
        Map<String, List<String>> index = new HashMap<>();
        Set<String> seen = new HashSet<>();
        for (String[] group : SYNONYM_GROUPS)
        {
            List<String> members = new ArrayList<>();
            for (String member : group)
            {
                String normalized = normalize(member);
                if (!members.contains(normalized))
                {
                    members.add(normalized);
                }
            }
            for (String member : members)
            {
                if (!seen.add(member))
                {
                    throw new IllegalStateException("Duplicate ingredient synonym: " + member);
                }
                index.put(member, new ArrayList<>(members));
            }
        }
        return index;
    }
}
