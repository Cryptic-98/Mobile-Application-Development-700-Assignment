package com.example.smartpantrymanager.models;

public class RecipeStep
{
    private Integer id;
    private int recipeId;
    private int stepNumber;
    private String instruction;

    public RecipeStep(int recipeId, int stepNumber, String instruction)
    {
        setRecipeId(recipeId);
        setStepNumber(stepNumber);
        setInstruction(instruction);
    }

    public RecipeStep(Integer id, int recipeId, int stepNumber, String instruction)
    {
        this(recipeId, stepNumber, instruction);
        setId(id);
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        if (id != null && id <= 0)
        {
            throw new IllegalArgumentException("id must be positive.");
        }
        this.id = id;
    }

    public int getRecipeId()
    {
        return recipeId;
    }

    public void setRecipeId(int recipeId)
    {
        if (recipeId <= 0)
        {
            throw new IllegalArgumentException("recipeId must be positive.");
        }
        this.recipeId = recipeId;
    }

    public int getStepNumber()
    {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber)
    {
        if (stepNumber <= 0)
        {
            throw new IllegalArgumentException("stepNumber must be positive.");
        }
        this.stepNumber = stepNumber;
    }

    public String getInstruction()
    {
        return instruction;
    }

    public void setInstruction(String instruction)
    {
        if (instruction == null || instruction.trim().isEmpty())
        {
            throw new IllegalArgumentException("instruction must not be blank.");
        }
        this.instruction = instruction.trim();
    }
}
