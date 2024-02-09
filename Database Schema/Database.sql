--
-- PostgreSQL database dump
--

-- Dumped from database version 15.3
-- Dumped by pg_dump version 16.0

-- Started on 2024-02-09 01:24:08

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 8 (class 2615 OID 16398)
-- Name: pgagent; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA pgagent;


ALTER SCHEMA pgagent OWNER TO postgres;

--
-- TOC entry 3503 (class 0 OID 0)
-- Dependencies: 8
-- Name: SCHEMA pgagent; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA pgagent IS 'pgAgent system tables';


--
-- TOC entry 2 (class 3079 OID 16384)
-- Name: adminpack; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS adminpack WITH SCHEMA pg_catalog;


--
-- TOC entry 3504 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION adminpack; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION adminpack IS 'administrative functions for PostgreSQL';


--
-- TOC entry 3 (class 3079 OID 16399)
-- Name: pgagent; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgagent WITH SCHEMA pgagent;


--
-- TOC entry 3505 (class 0 OID 0)
-- Dependencies: 3
-- Name: EXTENSION pgagent; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgagent IS 'A PostgreSQL job scheduler';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 239 (class 1259 OID 27245)
-- Name: subscribers; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.subscribers (
    subscriber_id integer NOT NULL,
    user_id integer,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.subscribers OWNER TO postgres;

--
-- TOC entry 238 (class 1259 OID 27244)
-- Name: subscribers_subscriber_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.subscribers_subscriber_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.subscribers_subscriber_id_seq OWNER TO postgres;

--
-- TOC entry 3506 (class 0 OID 0)
-- Dependencies: 238
-- Name: subscribers_subscriber_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.subscribers_subscriber_id_seq OWNED BY public.subscribers.subscriber_id;


--
-- TOC entry 243 (class 1259 OID 27266)
-- Name: user_zip_codes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_zip_codes (
    user_zip_code_id integer NOT NULL,
    user_id integer,
    zip_code_id integer,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.user_zip_codes OWNER TO postgres;

--
-- TOC entry 242 (class 1259 OID 27265)
-- Name: user_zip_codes_user_zip_code_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.user_zip_codes_user_zip_code_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user_zip_codes_user_zip_code_id_seq OWNER TO postgres;

--
-- TOC entry 3507 (class 0 OID 0)
-- Dependencies: 242
-- Name: user_zip_codes_user_zip_code_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.user_zip_codes_user_zip_code_id_seq OWNED BY public.user_zip_codes.user_zip_code_id;


--
-- TOC entry 245 (class 1259 OID 27284)
-- Name: user_zip_delivery; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_zip_delivery (
    user_zip_delivery_id integer NOT NULL,
    user_id integer,
    zip_code_id integer,
    delivery_time character varying NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.user_zip_delivery OWNER TO postgres;

--
-- TOC entry 244 (class 1259 OID 27283)
-- Name: user_zip_delivery_user_zip_delivery_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.user_zip_delivery_user_zip_delivery_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user_zip_delivery_user_zip_delivery_id_seq OWNER TO postgres;

--
-- TOC entry 3508 (class 0 OID 0)
-- Dependencies: 244
-- Name: user_zip_delivery_user_zip_delivery_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.user_zip_delivery_user_zip_delivery_id_seq OWNED BY public.user_zip_delivery.user_zip_delivery_id;


--
-- TOC entry 237 (class 1259 OID 27236)
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    user_id integer NOT NULL,
    phone_number character varying(15) NOT NULL,
    password_hash character varying(60) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.users OWNER TO postgres;

--
-- TOC entry 236 (class 1259 OID 27235)
-- Name: users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_user_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_user_id_seq OWNER TO postgres;

--
-- TOC entry 3509 (class 0 OID 0)
-- Dependencies: 236
-- Name: users_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_user_id_seq OWNED BY public.users.user_id;


--
-- TOC entry 246 (class 1259 OID 27305)
-- Name: weather_cache; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.weather_cache (
    cache_key character varying(255) NOT NULL,
    subscriber_id integer,
    location_name character varying(100) NOT NULL,
    date date NOT NULL,
    max_temperature integer NOT NULL,
    min_temperature integer NOT NULL,
    weather_condition character varying(100) NOT NULL,
    chance_of_rain integer,
    wind_speed character varying(20),
    wind_direction character varying(50),
    sunrise_time character varying,
    sunset_time character varying,
    expiration timestamp without time zone,
    zip_code_id integer
);


ALTER TABLE public.weather_cache OWNER TO postgres;

--
-- TOC entry 241 (class 1259 OID 27257)
-- Name: zip_codes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.zip_codes (
    zip_code_id integer NOT NULL,
    zip_code character varying(10) NOT NULL,
    CONSTRAINT check_length CHECK ((length((zip_code)::text) = 5))
);


ALTER TABLE public.zip_codes OWNER TO postgres;

--
-- TOC entry 240 (class 1259 OID 27256)
-- Name: zip_codes_zip_code_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.zip_codes_zip_code_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.zip_codes_zip_code_id_seq OWNER TO postgres;

--
-- TOC entry 3510 (class 0 OID 0)
-- Dependencies: 240
-- Name: zip_codes_zip_code_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.zip_codes_zip_code_id_seq OWNED BY public.zip_codes.zip_code_id;


--
-- TOC entry 3289 (class 2604 OID 27248)
-- Name: subscribers subscriber_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subscribers ALTER COLUMN subscriber_id SET DEFAULT nextval('public.subscribers_subscriber_id_seq'::regclass);


--
-- TOC entry 3292 (class 2604 OID 27269)
-- Name: user_zip_codes user_zip_code_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_zip_codes ALTER COLUMN user_zip_code_id SET DEFAULT nextval('public.user_zip_codes_user_zip_code_id_seq'::regclass);


--
-- TOC entry 3294 (class 2604 OID 27287)
-- Name: user_zip_delivery user_zip_delivery_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_zip_delivery ALTER COLUMN user_zip_delivery_id SET DEFAULT nextval('public.user_zip_delivery_user_zip_delivery_id_seq'::regclass);


--
-- TOC entry 3287 (class 2604 OID 27239)
-- Name: users user_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN user_id SET DEFAULT nextval('public.users_user_id_seq'::regclass);


--
-- TOC entry 3291 (class 2604 OID 27260)
-- Name: zip_codes zip_code_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.zip_codes ALTER COLUMN zip_code_id SET DEFAULT nextval('public.zip_codes_zip_code_id_seq'::regclass);


--
-- TOC entry 3336 (class 2606 OID 27250)
-- Name: subscribers subscribers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subscribers
    ADD CONSTRAINT subscribers_pkey PRIMARY KEY (subscriber_id);


--
-- TOC entry 3342 (class 2606 OID 27272)
-- Name: user_zip_codes user_zip_codes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_zip_codes
    ADD CONSTRAINT user_zip_codes_pkey PRIMARY KEY (user_zip_code_id);


--
-- TOC entry 3344 (class 2606 OID 27292)
-- Name: user_zip_delivery user_zip_delivery_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_zip_delivery
    ADD CONSTRAINT user_zip_delivery_pkey PRIMARY KEY (user_zip_delivery_id);


--
-- TOC entry 3346 (class 2606 OID 27294)
-- Name: user_zip_delivery user_zip_delivery_user_id_zip_code_id_delivery_time_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_zip_delivery
    ADD CONSTRAINT user_zip_delivery_user_id_zip_code_id_delivery_time_key UNIQUE (user_id, zip_code_id, delivery_time);


--
-- TOC entry 3332 (class 2606 OID 27243)
-- Name: users users_phone_number_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_phone_number_key UNIQUE (phone_number);


--
-- TOC entry 3334 (class 2606 OID 27241)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- TOC entry 3348 (class 2606 OID 27311)
-- Name: weather_cache weather_cache_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.weather_cache
    ADD CONSTRAINT weather_cache_pkey PRIMARY KEY (cache_key);


--
-- TOC entry 3338 (class 2606 OID 27262)
-- Name: zip_codes zip_codes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.zip_codes
    ADD CONSTRAINT zip_codes_pkey PRIMARY KEY (zip_code_id);


--
-- TOC entry 3340 (class 2606 OID 27264)
-- Name: zip_codes zip_codes_zip_code_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.zip_codes
    ADD CONSTRAINT zip_codes_zip_code_key UNIQUE (zip_code);


--
-- TOC entry 3349 (class 2606 OID 27251)
-- Name: subscribers subscribers_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subscribers
    ADD CONSTRAINT subscribers_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 3350 (class 2606 OID 27273)
-- Name: user_zip_codes user_zip_codes_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_zip_codes
    ADD CONSTRAINT user_zip_codes_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 3351 (class 2606 OID 27278)
-- Name: user_zip_codes user_zip_codes_zip_code_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_zip_codes
    ADD CONSTRAINT user_zip_codes_zip_code_id_fkey FOREIGN KEY (zip_code_id) REFERENCES public.zip_codes(zip_code_id);


--
-- TOC entry 3352 (class 2606 OID 27295)
-- Name: user_zip_delivery user_zip_delivery_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_zip_delivery
    ADD CONSTRAINT user_zip_delivery_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 3353 (class 2606 OID 27300)
-- Name: user_zip_delivery user_zip_delivery_zip_code_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_zip_delivery
    ADD CONSTRAINT user_zip_delivery_zip_code_id_fkey FOREIGN KEY (zip_code_id) REFERENCES public.zip_codes(zip_code_id);


--
-- TOC entry 3354 (class 2606 OID 27312)
-- Name: weather_cache weather_cache_subscriber_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.weather_cache
    ADD CONSTRAINT weather_cache_subscriber_id_fkey FOREIGN KEY (subscriber_id) REFERENCES public.subscribers(subscriber_id);


--
-- TOC entry 3355 (class 2606 OID 27317)
-- Name: weather_cache weather_cache_zip_code_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.weather_cache
    ADD CONSTRAINT weather_cache_zip_code_id_fkey FOREIGN KEY (zip_code_id) REFERENCES public.zip_codes(zip_code_id);


-- Completed on 2024-02-09 01:24:09

--
-- PostgreSQL database dump complete
--

