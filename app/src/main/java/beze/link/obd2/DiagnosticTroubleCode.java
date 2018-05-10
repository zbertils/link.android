package beze.link.obd2;

public class DiagnosticTroubleCode
{
    /// <summary>
    /// Enumerated values of known vehicle computers.
    /// </summary>
    public enum VehicleComputer
    {
        Unknown,
        Powertrain,
        Chassis,
        Body,
        Network
    }

    /// <summary>
    /// Enumerated values of known diagnostic trouble code types.
    /// </summary>
    public enum CodeType
    {
        Set,
        Permanent,
        Pending,
        Cleared,
        StatusCheck
    }

    /// <summary>
    /// The formal diagnostic trouble code, e.g. "P0670".
    /// </summary>
    public final String Code;

    /// <summary>
    /// The type of diagnostic trouble code.
    /// </summary>
    public final CodeType Type;

    /// <summary>
    /// The description of the diagnostic trouble code, if available. This value may be null or String.Empty.
    /// </summary>
    public String Description;

    /// <summary>
    /// The computer associated with the formal diagnostic trouble code.
    /// </summary>
    public final VehicleComputer Computer;


    /// <summary>
    /// Creates a new DiagnosticTroubleCode object.
    /// </summary>
    /// <param name="elm327code"> The informal ELM327 code, e.g. "0670" where the first digit represents an encoded letter and number. </param>
    /// <param name="type"> The type of code. This indirectly represents how the code was retrieved from the vehicle. </param>
    /// <param name="description"> The description of the diagnostic trouble code, if available. </param>
    public DiagnosticTroubleCode(String elm327code, CodeType type, String description)
    {
        this.Code = DecodeFullDtcFromElm327Code(elm327code);
        this.Type = type;
        this.Description = description;
        this.Computer = GetControllerFromCodeLetter(this.Code);
    }

    /// <summary>
    /// Creates a new DiagnosticTroubleCode object.
    /// </summary>
    /// <param name="elm327code"> The informal ELM327 code, e.g. "0670" where the first digit represents an encoded letter and number. </param>
    /// <param name="type"> The type of code. This indirectly represents how the code was retrieved from the vehicle. </param>
    public DiagnosticTroubleCode(String elm327code, CodeType type)
    {
        this.Code = DecodeFullDtcFromElm327Code(elm327code);
        this.Type = type;
        this.Description = "";
        this.Computer = GetControllerFromCodeLetter(this.Code);
    }

    /// <summary>
    /// Gets the enumerated VehicleComputer value from a valid OBD2 diagnostic code.
    /// </summary>
    /// <param name="code"> The full code value, e.g. "P0176". </param>
    /// <returns> The VehicleComputer enumerated value for the given code. </returns>
    public static VehicleComputer GetControllerFromCodeLetter(String code)
    {
        if (code != null && !code.isEmpty() && code.length() > 0)
        {
            switch (code.toUpperCase().charAt(0))
            {
                case 'P' :
                    return VehicleComputer.Powertrain;
                case 'C' :
                    return VehicleComputer.Chassis;
                case 'B' :
                    return VehicleComputer.Body;
                case 'U' :
                    return VehicleComputer.Network;
                default :
                    return VehicleComputer.Unknown;
            }
        }

        return VehicleComputer.Unknown;
    }

    /// <summary>
    /// Converts a Mode $19 encoded leading letter or number into the associated DTC leading letter and number.
    /// </summary>
    /// <param name="codeLetter"> The first digit of the two byte Mode $19 response. </param>
    /// <returns> The leading DTC letter and number, e.g. "P1", if the code letter is understood and null otherwise. </returns>
    public String DecodeFullDtcFromElm327Code(String elm327code)
    {
        char codeLetter = elm327code.charAt(0);
        String prependedCode = "";
        switch (codeLetter)
        {
            case '0': prependedCode = "P0"; break;
            case '1': prependedCode = "P1"; break;
            case '2': prependedCode = "P2"; break;
            case '3': prependedCode = "P3"; break;
            case '4': prependedCode = "B0"; break;
            case '5': prependedCode = "B1"; break;
            case '6': prependedCode = "B2"; break;
            case '7': prependedCode = "B3"; break;
            case '8': prependedCode = "C0"; break;
            case '9': prependedCode = "C1"; break;
            case 'A': prependedCode = "C2"; break;
            case 'B': prependedCode = "C3"; break;
            case 'C': prependedCode = "U0"; break;
            case 'D': prependedCode = "U1"; break;
            case 'E': prependedCode = "U2"; break;
            case 'F': prependedCode = "U3"; break;
            default: prependedCode = "??"; break; // this should never happen
        }

        return prependedCode + elm327code.substring(1);
    }

}
