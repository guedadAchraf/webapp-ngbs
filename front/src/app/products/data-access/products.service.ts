import { Injectable, inject, signal } from "@angular/core";
import { Product } from "./product.model";
import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { catchError, Observable, of, tap } from "rxjs";
import { environment } from "../../../environments/environment";

@Injectable({
    providedIn: "root"
}) export class ProductsService {

    private readonly http = inject(HttpClient);
    private readonly path = `${environment.apiUrl}/api/products`;
    
    private readonly _products = signal<Product[]>([]);

    public readonly products = this._products.asReadonly();

    public get(): Observable<Product[]> {
        return this.http.get<Product[]>(this.path).pipe(
            catchError((error) => {
                console.error('API Error:', error);
                if (!environment.production) {
                    return this.http.get<Product[]>("assets/products.json");
                }
                throw error;
            }),
            tap((products) => this._products.set(products)),
        );
    }

    public create(product: Product): Observable<Product> {
        const productToCreate = {
            ...product,
            code: product.code || this.generateProductCode(),
            createdAt: Date.now(),
            updatedAt: Date.now()
        };

        const cleanProduct = Object.fromEntries(
            Object.entries(productToCreate).filter(([_, v]) => v != null)
        ) as unknown as Product;

        return this.http.post<Product>(this.path, cleanProduct).pipe(
            catchError((error: HttpErrorResponse) => {
                console.error('API Error:', {
                    status: error.status,
                    message: error.error?.message || error.message,
                    details: error.error
                });
                throw new Error(error.error?.message || 'Failed to create product');
            }),
            tap((newProduct) => {
                this._products.update(products => [newProduct, ...products]);
            }),
        );
    }

    private generateProductCode(): string {
        const chars = 'abcdefghijklmnopqrstuvwxyz0123456789';
        const length = 8;
        return Array.from(
            { length }, 
            () => chars.charAt(Math.floor(Math.random() * chars.length))
        ).join('');
    }

    public update(product: Product): Observable<Product> {
        return this.http.patch<Product>(`${this.path}/${product.id}`, product).pipe(
            catchError((error) => {
                console.error('API Error:', error);
                throw error;
            }),
            tap((updatedProduct) => this._products.update(products => 
                products.map(p => p.id === updatedProduct.id ? updatedProduct : p)
            )),
        );
    }

    public delete(productId: number): Observable<void> {
        return this.http.delete<void>(`${this.path}/${productId}`).pipe(
            catchError((error) => {
                console.error('API Error:', error);
                throw error;
            }),
            tap(() => this._products.update(products => 
                products.filter(product => product.id !== productId)
            )),
        );
    }
}