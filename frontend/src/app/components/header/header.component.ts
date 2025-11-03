import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterModule, Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { gql } from '@apollo/client/core';
import { Apollo } from 'apollo-angular';

const GET_CURRENT_USER_PROFILE = gql`
  query GetCurrentUserProfile {
    getCurrentUserProfile {
      email
      firstName
      lastName
      aadharNumber
      city
      phoneNumber
    }
  }
`;


@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive, RouterModule, CommonModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  toHome() {
    document.getElementById("hero")?.scrollIntoView({behavior:"smooth"});
  }

  toDestinations() {
    document.getElementById("destinations")?.scrollIntoView({behavior:"smooth"});
  }

  toBookings() {
    document.getElementById("bookings")?.scrollIntoView({behavior:"smooth"});
  }

  firstName = '';
  lastName = '';
  initials = '';
  dropdownOpen = false;

  toggleDropdown() {
    this.dropdownOpen = !this.dropdownOpen;
  }



  constructor(private router: Router, private apollo: Apollo) {}

  ngOnInit() {
    const token = localStorage.getItem('authToken');
    // console.log(token)
    if (token) {
      this.apollo
        .query({
          query: GET_CURRENT_USER_PROFILE,
          context: {
            headers: new HttpHeaders({
              Authorization: `Bearer ${token}`
            })
          },
          fetchPolicy: 'no-cache'
        })
        .subscribe({
          next: (result: any) => {
            // console.log(result);
            const profile = result.data.getCurrentUserProfile;
            this.firstName = profile.firstName;
            this.lastName = profile.lastName;
            this.initials = this.getInitials(this.firstName, this.lastName);
          },
          error: (err) => {
            console.error('Error fetching profile:', err);
          }
        });
    }
  }

  getInitials(first: string, last: string) {
    return (first.charAt(0) + last.charAt(0)).toUpperCase();
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login/user']);
  }
}
