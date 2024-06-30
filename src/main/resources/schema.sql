create table gvggroup.products (
    id integer not null,
    created timestamp(6),
    modified timestamp(6),
    name varchar(255),
    price numeric(38,2),
    quantity integer not null, primary key (id));

create table gvggroup.order_requests (
     id integer not null,
     created timestamp(6),
     info varchar(255),
     modified timestamp(6),
     status varchar(255) check (status in ('REQUESTED','COMPLETED','REJECTED','FAILED','EXPIRED')),
     order_id uuid, primary key (id));

create table gvggroup.orders (
    id uuid not null,
    created timestamp(6),
    modified timestamp(6),
    price numeric(38,2),
    quantity integer not null, status varchar(255) check (status in ('PENDING','APPROVED','REJECTED')),
    user_id integer not null, product_id integer, primary key (id));

create sequence order_requests_seq start with 1 increment by 50;

create sequence products_seq start with 1 increment by 50;

alter table if exists gvggroup.order_requests
    add constraint FK_order_id_refers_to_orders foreign key (order_id) references gvggroup.orders;

alter table if exists gvggroup.orders
    add constraint FK_product_id_refers_to_products foreign key (product_id) references gvggroup.products;