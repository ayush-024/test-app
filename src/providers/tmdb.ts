import axios from 'axios';
import { TMDB_API_KEY } from '@env';
import { TmdbMovie, TmdbShow, TmdbMulti } from '../types/tmdb';

const api = axios.create({
  baseURL: 'https://api.themoviedb.org/3',
  params: { api_key: TMDB_API_KEY, language: 'en-US' },
});

export const getPopularMovies = () =>
  api.get<{ results: TmdbMovie[] }>('/movie/popular').then(r => r.data.results);

export const getPopularShows = () =>
  api.get<{ results: TmdbShow[] }>('/tv/popular').then(r => r.data.results);

export const searchTMDB = (query: string) =>
  api.get<{ results: TmdbMulti[] }>('/search/multi', { params: { query } })
    .then(r => r.data.results);
