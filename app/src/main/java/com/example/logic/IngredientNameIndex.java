package com.example.smartpantrymanager.logic;

import com.example.smartpantrymanager.models.Ingredient;
import com.example.smartpantrymanager.models.IngredientAlias;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
















public final class IngredientNameIndex
{
    private final Map<String, Integer> idsByName;
    private final Map<String, Integer> idsByAlias;

    private IngredientNameIndex(Map<String, Integer> idsByName, Map<String, Integer> idsByAlias)
    {
        this.idsByName = idsByName;
        this.idsByAlias = idsByAlias;
    }






    public static IngredientNameIndex build(List<Ingredient> ingredients, List<IngredientAlias> aliases)
    {
        Map<String, Integer> idsByName = new HashMap<>();
        Map<String, Integer> idsByAlias = new HashMap<>();
        if (ingredients != null)
        {
            for (Ingredient ingredient : ingredients)
            {
                if (ingredient == null || ingredient.getId() == null)
                {
                    continue;
                }
                String normalized = IngredientSynonyms.normalize(ingredient.getCanonicalName());
                if (!normalized.isEmpty())
                {
                    idsByName.putIfAbsent(normalized, ingredient.getId());
                }
                String stored = ingredient.getNormalizedName();
                if (stored != null && !stored.trim().isEmpty())
                {
                    idsByName.putIfAbsent(IngredientSynonyms.normalize(stored), ingredient.getId());
                }
            }
        }
        if (aliases != null)
        {
            for (IngredientAlias alias : aliases)
            {
                if (alias == null)
                {
                    continue;
                }
                String normalized = IngredientSynonyms.normalize(alias.getNormalizedAlias());
                if (!normalized.isEmpty())
                {
                    idsByAlias.putIfAbsent(normalized, alias.getIngredientId());
                }
            }
        }
        return new IngredientNameIndex(idsByName, idsByAlias);
    }







    public Integer resolve(String name)
    {
        String normalized = IngredientSynonyms.normalize(name);
        if (normalized.isEmpty())
        {
            return null;
        }
        Integer direct = idsByName.get(normalized);
        if (direct != null)
        {
            return direct;
        }
        Integer alias = idsByAlias.get(normalized);
        if (alias != null)
        {
            return alias;
        }
        for (String candidate : IngredientSynonyms.expand(normalized))
        {
            if (candidate.equals(normalized))
            {
                continue;
            }
            Integer match = idsByName.get(candidate);
            if (match != null)
            {
                return match;
            }
            match = idsByAlias.get(candidate);
            if (match != null)
            {
                return match;
            }
        }
        return null;
    }











    public Integer resolveToPantryIngredient(String name, Set<Integer> pantryIngredientIds)
    {
        if (pantryIngredientIds == null || pantryIngredientIds.isEmpty())
        {
            return null;
        }
        String normalized = IngredientSynonyms.normalize(name);
        if (normalized.isEmpty())
        {
            return null;
        }
        for (String candidate : IngredientSynonyms.expand(normalized))
        {
            Integer match = idsByName.get(candidate);
            if (match == null)
            {
                match = idsByAlias.get(candidate);
            }
            if (match != null && pantryIngredientIds.contains(match))
            {
                return match;
            }
        }
        return null;
    }





    public static String canonicalTerm(String name)
    {
        return IngredientSynonyms.canonicalTerm(name);
    }





    public static List<String> expandAll(Collection<String> names)
    {
        List<String> result = new ArrayList<>();
        if (names == null)
        {
            return Collections.unmodifiableList(result);
        }
        for (String name : names)
        {
            for (String candidate : IngredientSynonyms.expand(name))
            {
                if (!result.contains(candidate))
                {
                    result.add(candidate);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }
}
