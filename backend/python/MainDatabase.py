import data_create.namespace_pop as popNameSpaces
import data_create.entity_pop as popEntities
import data_create.clean_table as clean
import data_create.var_pop as popVariables

def main():
    # Clean tables in proper order (children first, then parents)
    clean.clean_table("Variables")    # Clean first (references Entities)
    #clean.clean_table("Constants")    # Clean second (references Entities)
    clean.clean_table("Functions")    # Clean third (references Classes and Namespaces)
    clean.clean_table("Classes")      # Clean fourth (references Namespaces)
    clean.clean_table("Entities")     # Clean fifth (references Namespaces)
    clean.clean_table("Namespaces")   # Clean last (parent table)
    
    # Populate namespaces
    popNameSpaces.populate_namespaces_from_output("../output")
    
    # Populate classes and functions based on namespaces
    popEntities.populate_entities_from_namespaces("../output")

    popVariables.populate_variables("../output")

if __name__ == "__main__":
    main()