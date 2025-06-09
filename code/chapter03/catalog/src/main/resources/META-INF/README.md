# JPA Schema Generation and Data Initialization

This project uses Jakarta Persistence's schema generation and SQL script loading capabilities for database initialization.

## Configuration

The database initialization is configured in `src/main/resources/META-INF/persistence.xml` with the following properties:

```xml
<!-- Schema generation -->
<property name="jakarta.persistence.schema-generation.database.action" value="drop-and-create"/>

<!-- Script generation -->
<property name="jakarta.persistence.schema-generation.scripts.action" value="drop-and-create"/>
<property name="jakarta.persistence.schema-generation.scripts.create-target" value="createDDL.ddl"/>
<property name="jakarta.persistence.schema-generation.scripts.drop-target" value="dropDDL.ddl"/>

<!-- Data initialization from SQL script -->
<property name="jakarta.persistence.sql-load-script-source" value="META-INF/sql/import.sql"/>
```

## How It Works

1. When the application starts, JPA will automatically:
   - Drop all existing tables (if they exist)
   - Create new tables based on your entity definitions
   - Generate DDL scripts in the target directory
   - Execute the SQL statements in `META-INF/sql/import.sql` to populate initial data

2. The `import.sql` file contains INSERT statements for the initial product data:
   ```sql
   INSERT INTO Product (id, name, description, price) VALUES (1, 'iPhone', 'Apple iPhone 15', 999.99);
   ...
   ```

## Benefits

- **Declarative**: No need for initialization code in Java
- **Repeatable**: Schema is always consistent with entity definitions
- **Version-controlled**: SQL scripts can be tracked in version control
- **Portable**: Works across different database providers
- **Transparent**: DDL scripts are generated for inspection

## Notes

- To disable this behavior in production, change the `database.action` value to `none` or use profiles
- You can also separate the creation schema script and data loading script if needed
- For versioned database migrations, consider tools like Flyway or Liquibase instead
