
/*tablenames, columns, column types*/
SELECT tablename, column_name, data_type FROM pg_tables t left join information_schema.columns c on c.table_name=t.tablename
WHERE tablename NOT LIKE 'pg_%' and tableowner='cleaner'
order by tablename, data_type, column_name;

/* all sequences*/
SELECT c.relname FROM pg_class c WHERE c.relkind = 'S';


/* tablenames only */
SELECT tablename FROM pg_tables t 
WHERE tablename NOT LIKE 'pg_%' and tableowner='cleaner'
order by tablename;
