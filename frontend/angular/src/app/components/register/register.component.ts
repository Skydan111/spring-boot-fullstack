import { Component } from '@angular/core';
import {Router} from "@angular/router";
import {CustomerRegistrationRequest} from "../../models/customer-registration-request";
import {CustomerService} from "../../services/customer/customer.service";
import {AuthenticationService} from "../../services/auth/authentication.service";
import {AuthenticationRequest} from "../../models/authentication-request";

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  errorMsg = '';
  customer: CustomerRegistrationRequest = {};

  constructor(
    private router: Router,
    private customerService: CustomerService,
    private authenticationService: AuthenticationService
  ) {
  }

  goToLogin() {
    this.router.navigate(['login']);
  }

  createAccount() {
    this.customerService.registerCustomer(this.customer)
      .subscribe({
        next: () => {
          const authRequest: AuthenticationRequest = {
            username: this.customer.email,
            password: this.customer.password
          }
          this.authenticationService.login(authRequest)
            .subscribe({
              next: (authRes) => {
                localStorage.setItem('user', JSON.stringify(authRes));
                this.router.navigate(['customers']);
              },
              error: (err) => {
                if (err.error.statusCode === 401) {
                  this.errorMsg = 'Login and / or password is incorrect';
                }
              }
            });
        }
      });
  }
}
