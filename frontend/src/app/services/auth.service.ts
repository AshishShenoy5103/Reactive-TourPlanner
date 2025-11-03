import { Injectable } from '@angular/core';
import { Apollo } from 'apollo-angular';
import { gql } from '@apollo/client/core';
import { map } from 'rxjs/operators';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private apollo: Apollo) {}

  register(userRegisterDTO: any): Observable<any> {
    const REGISTER_MUTATION = gql`
      mutation RegisterUser($userRegisterDTO: UserRegisterDTO!) {
        registerUser(userRegisterDTO: $userRegisterDTO) {
          email
          firstName
          lastName
          aadharNumber
          city
          phoneNumber
        }
      }
    `;

    return this.apollo.mutate({
      mutation: REGISTER_MUTATION,
      variables: { userRegisterDTO }
    }).pipe(map((result: any) => result.data));
  }

  login(email: string, password: string): Observable<{ token: string; error: string | null }> {
    const LOGIN_MUTATION = gql`
      mutation LoginUser($email: String!, $password: String!) {
        loginUser(email: $email, password: $password) {
          token
          error
        }
      }
    `;

    return this.apollo.mutate<{ loginUser: { token: string; error: string | null } }>({
      mutation: LOGIN_MUTATION,
      variables: { email, password }
    }).pipe(
      map(result => {
        const loginData = result.data?.loginUser!;
        if (loginData.token) localStorage.setItem('authToken', loginData.token);
        return loginData;
      })
    );
  }
}
