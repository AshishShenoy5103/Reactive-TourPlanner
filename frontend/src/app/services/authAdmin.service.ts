import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Apollo } from 'apollo-angular';
import { map } from 'rxjs/operators';
import { gql } from '@apollo/client/core';

@Injectable({
  providedIn: 'root'
})
export class AuthAdminService {
  constructor(private http: HttpClient, private apollo: Apollo) {}

  login(email: string, password: string): Observable<{ token: string; error: string | null }> {
      const LOGIN_MUTATION = gql`
        mutation LoginAdmin($email: String!, $password: String!) {
          loginAdmin(email: $email, password: $password) {
            token
            error
          }
        }
      `;

      return this.apollo.mutate<{ loginAdmin: { token: string; error: string | null } }>({
        mutation: LOGIN_MUTATION,
        variables: { email, password }
      }).pipe(
        map(result => {
          const loginData = result.data?.loginAdmin!;
          if (loginData.token) localStorage.setItem('authToken', loginData.token);
          return loginData;
        })
      );
    }

}
